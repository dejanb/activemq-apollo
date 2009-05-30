/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.broker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.activemq.broker.db.BrokerDatabase;
import org.apache.activemq.broker.db.RestoreListener;
import org.apache.activemq.broker.db.SaveableQueueElement;
import org.apache.activemq.broker.store.QueueDescriptor;
import org.apache.activemq.broker.store.Store.QueueQueryResult;
import org.apache.activemq.dispatch.IDispatcher;
import org.apache.activemq.flow.ISourceController;
import org.apache.activemq.flow.PrioritySizeLimiter;
import org.apache.activemq.flow.SizeLimiter;
import org.apache.activemq.queue.ExclusivePersistentQueue;
import org.apache.activemq.queue.IPartitionedQueue;
import org.apache.activemq.queue.IQueue;
import org.apache.activemq.queue.PartitionedQueue;
import org.apache.activemq.queue.PersistencePolicy;
import org.apache.activemq.queue.QueueStore;
import org.apache.activemq.queue.SharedPriorityQueue;
import org.apache.activemq.queue.SharedQueue;
import org.apache.activemq.queue.SharedQueueOld;
import org.apache.activemq.util.Mapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BrokerQueueStore implements QueueStore<Long, MessageDelivery> {

    private static final Log LOG = LogFactory.getLog(BrokerQueueStore.class);
    private static final boolean USE_OLD_QUEUE = false;
    private static final boolean USE_PRIORITY_QUEUES = true;

    private BrokerDatabase database;
    private IDispatcher dispatcher;

    private static final Mapper<Long, MessageDelivery> EXPIRATION_MAPPER = new Mapper<Long, MessageDelivery>() {
        public Long map(MessageDelivery element) {
            return element.getExpiration();
        }
    };

    private static final Mapper<Integer, MessageDelivery> SIZE_MAPPER = new Mapper<Integer, MessageDelivery>() {
        public Integer map(MessageDelivery element) {
            return element.getFlowLimiterSize();
        }
    };

    private final short PARTITION_TYPE = 0;
    private final short SHARED_QUEUE_TYPE = 1;
    private final short DURABLE_QUEUE_TYPE = 2;

    private final HashMap<String, IQueue<Long, MessageDelivery>> sharedQueues = new HashMap<String, IQueue<Long, MessageDelivery>>();
    private final HashMap<String, ExclusivePersistentQueue<Long, MessageDelivery>> durableQueues = new HashMap<String, ExclusivePersistentQueue<Long, MessageDelivery>>();

    private Mapper<Integer, MessageDelivery> partitionMapper;

    private static final int DEFAULT_SHARED_QUEUE_PAGING_THRESHOLD = 1024 * 1024 * 1;
    private static final int DEFAULT_SHARED_QUEUE_RESUME_THRESHOLD = 1;
    // Be default we don't page out elements to disk.
    private static final int DEFAULT_SHARED_QUEUE_SIZE = DEFAULT_SHARED_QUEUE_PAGING_THRESHOLD;
    //private static final int DEFAULT_SHARED_QUEUE_SIZE = 1024 * 1024 * 10;

    private static final PersistencePolicy<MessageDelivery> SHARED_QUEUE_PERSISTENCE_POLICY = new PersistencePolicy<MessageDelivery>() {

        private static final boolean PAGING_ENABLED = DEFAULT_SHARED_QUEUE_SIZE > DEFAULT_SHARED_QUEUE_PAGING_THRESHOLD;

        public boolean isPersistent(MessageDelivery elem) {
            return elem.isPersistent();
        }

        public boolean isPageOutPlaceHolders() {
            return true;
        }

        public boolean isPagingEnabled() {
            return PAGING_ENABLED;
        }

        public int getPagingInMemorySize() {
            return DEFAULT_SHARED_QUEUE_PAGING_THRESHOLD;
        }

        public boolean isThrottleSourcesToMemoryLimit() {
            // Keep the queue in memory.
            return true;
        }

        public int getDisconnectedThrottleRate() {
            // By default don't throttle consumers when disconnected.
            return 0;
        }

        public int getRecoveryBias() {
            return 8;
        }
    };

    private static final int DEFAULT_DURABLE_QUEUE_PAGING_THRESHOLD = 100 * 1024 * 1;
    private static final int DEFAULT_DURABLE_QUEUE_RESUME_THRESHOLD = 1;
    // Be default we don't page out elements to disk.
    private static final int DEFAULT_DURABLE_QUEUE_SIZE = DEFAULT_DURABLE_QUEUE_PAGING_THRESHOLD;

    private static final PersistencePolicy<MessageDelivery> DURABLE_QUEUE_PERSISTENCE_POLICY = new PersistencePolicy<MessageDelivery>() {

        private static final boolean PAGING_ENABLED = DEFAULT_DURABLE_QUEUE_SIZE > DEFAULT_DURABLE_QUEUE_PAGING_THRESHOLD;

        public boolean isPersistent(MessageDelivery elem) {
            return elem.isPersistent();
        }

        public boolean isPageOutPlaceHolders() {
            return true;
        }

        public boolean isPagingEnabled() {
            return PAGING_ENABLED;
        }

        public int getPagingInMemorySize() {
            return DEFAULT_DURABLE_QUEUE_PAGING_THRESHOLD;
        }

        public boolean isThrottleSourcesToMemoryLimit() {
            // Keep the queue in memory.
            return true;
        }

        public int getDisconnectedThrottleRate() {
            // By default don't throttle consumers when disconnected.
            return 0;
        }

        public int getRecoveryBias() {
            return 8;
        }
    };

    public static final Mapper<Integer, MessageDelivery> PRIORITY_MAPPER = new Mapper<Integer, MessageDelivery>() {
        public Integer map(MessageDelivery element) {
            return element.getPriority();
        }
    };

    static public final Mapper<Long, MessageDelivery> KEY_MAPPER = new Mapper<Long, MessageDelivery>() {
        public Long map(MessageDelivery element) {
            return element.getStoreTracking();
        }
    };

    static public final Mapper<Integer, MessageDelivery> PARTITION_MAPPER = new Mapper<Integer, MessageDelivery>() {
        public Integer map(MessageDelivery element) {
            // we modulo 10 to have at most 10 partitions which the producers
            // gets split across.
            return (int) (element.getProducerId().hashCode() % 10);
        }
    };

    public void setDatabase(BrokerDatabase database) {
        this.database = database;
    }

    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void loadQueues() throws Exception {

        // Load shared queues
        Iterator<QueueQueryResult> results = database.listQueues(SHARED_QUEUE_TYPE);
        while (results.hasNext()) {
            QueueQueryResult loaded = results.next();
            IQueue<Long, MessageDelivery> queue = createRestoredQueue(null, loaded);
            sharedQueues.put(queue.getDescriptor().getQueueName().toString(), queue);
            LOG.info("Loaded Queue " + queue.getResourceName() + " Messages: " + queue.getEnqueuedCount() + " Size: " + queue.getEnqueuedSize());
        }

        // Load durable queues
        results = database.listQueues(DURABLE_QUEUE_TYPE);
        while (results.hasNext()) {
            QueueQueryResult loaded = results.next();
            ExclusivePersistentQueue<Long, MessageDelivery> queue = createRestoredDurableQueue(loaded);
            durableQueues.put(queue.getDescriptor().getQueueName().toString(), queue);
            LOG.info("Loaded Durable " + queue.getResourceName() + " Messages: " + queue.getEnqueuedCount() + " Size: " + queue.getEnqueuedSize());

        }
    }

    private IQueue<Long, MessageDelivery> createRestoredQueue(IPartitionedQueue<Long, MessageDelivery> parent, QueueQueryResult loaded) throws IOException {

        IQueue<Long, MessageDelivery> queue;
        if (parent != null) {
            queue = parent.createPartition(loaded.getDescriptor().getPartitionKey());
        } else {
            queue = createSharedQueueInternal(loaded.getDescriptor().getQueueName().toString(), loaded.getDescriptor().getQueueType());
        }

        queue.initialize(loaded.getFirstSequence(), loaded.getLastSequence(), loaded.getCount(), loaded.getSize());

        // Creat the child queues
        Collection<QueueQueryResult> children = loaded.getPartitions();
        if (children != null) {
            try {
                IPartitionedQueue<Long, MessageDelivery> pQueue = (IPartitionedQueue<Long, MessageDelivery>) queue;
                for (QueueQueryResult child : children) {
                    createRestoredQueue(pQueue, child);
                }
            } catch (ClassCastException cce) {
                LOG.error("Loaded partition for unpartitionable queue: " + queue.getResourceName());
                throw cce;
            }
        }

        return queue;

    }

    private ExclusivePersistentQueue<Long, MessageDelivery> createRestoredDurableQueue(QueueQueryResult loaded) throws IOException {

        ExclusivePersistentQueue<Long, MessageDelivery> queue = createDurableQueueInternal(loaded.getDescriptor().getQueueName().toString(), loaded.getDescriptor().getQueueType());
        queue.initialize(loaded.getFirstSequence(), loaded.getLastSequence(), loaded.getCount(), loaded.getSize());

        //TODO implement this for priority queue:
        // Create the child queues
        /*
         * Collection<QueueQueryResult> children = loaded.getPartitions(); if
         * (children != null) { try { IPartitionedQueue<Long, MessageDelivery>
         * pQueue = (IPartitionedQueue<Long, MessageDelivery>) queue; for
         * (QueueQueryResult child : children) { createRestoredQueue(pQueue,
         * child); } } catch (ClassCastException cce) {
         * LOG.error("Loaded partition for unpartitionable queue: " +
         * queue.getResourceName()); throw cce; } }
         */

        return queue;

    }

    public Collection<IQueue<Long, MessageDelivery>> getSharedQueues() {
        synchronized (this) {
            Collection<IQueue<Long, MessageDelivery>> c = sharedQueues.values();
            ArrayList<IQueue<Long, MessageDelivery>> ret = new ArrayList<IQueue<Long, MessageDelivery>>(c.size());
            ret.addAll(c);
            return ret;
        }
    }

    public ExclusivePersistentQueue<Long, MessageDelivery> createDurableQueue(String name) {
        ExclusivePersistentQueue<Long, MessageDelivery> queue = null;
        synchronized (this) {
            queue = durableQueues.get(name);
            if (queue == null) {
                queue = createDurableQueueInternal(name, USE_PRIORITY_QUEUES ? QueueDescriptor.SHARED_PRIORITY : QueueDescriptor.SHARED);
                queue.getDescriptor().setApplicationType(DURABLE_QUEUE_TYPE);
                queue.initialize(0, 0, 0, 0);
                durableQueues.put(name, queue);
                addQueue(queue.getDescriptor());
            }
        }

        return queue;
    }

    public Collection<ExclusivePersistentQueue<Long, MessageDelivery>> getDurableQueues() {
        synchronized (this) {
            Collection<ExclusivePersistentQueue<Long, MessageDelivery>> c = durableQueues.values();
            ArrayList<ExclusivePersistentQueue<Long, MessageDelivery>> ret = new ArrayList<ExclusivePersistentQueue<Long, MessageDelivery>>(c.size());
            ret.addAll(c);
            return ret;
        }
    }

    public IQueue<Long, MessageDelivery> createSharedQueue(String name) {

        IQueue<Long, MessageDelivery> queue = null;
        synchronized (this) {
            queue = sharedQueues.get(name);
            if (queue == null) {
                queue = createSharedQueueInternal(name, USE_PRIORITY_QUEUES ? QueueDescriptor.SHARED_PRIORITY : QueueDescriptor.SHARED);
                queue.getDescriptor().setApplicationType(SHARED_QUEUE_TYPE);
                queue.initialize(0, 0, 0, 0);
                sharedQueues.put(name, queue);
                addQueue(queue.getDescriptor());
            }
        }

        return queue;
    }

    private ExclusivePersistentQueue<Long, MessageDelivery> createDurableQueueInternal(final String name, short type) {
        ExclusivePersistentQueue<Long, MessageDelivery> queue;

        SizeLimiter<MessageDelivery> limiter = new SizeLimiter<MessageDelivery>(DEFAULT_DURABLE_QUEUE_SIZE, DEFAULT_DURABLE_QUEUE_RESUME_THRESHOLD) {
            @Override
            public int getElementSize(MessageDelivery elem) {
                return elem.getFlowLimiterSize();
            }
        };
        queue = new ExclusivePersistentQueue<Long, MessageDelivery>(name, limiter);
        queue.setDispatcher(dispatcher);
        queue.setStore(this);
        queue.setPersistencePolicy(DURABLE_QUEUE_PERSISTENCE_POLICY);
        queue.setExpirationMapper(EXPIRATION_MAPPER);
        return queue;
    }

    private IQueue<Long, MessageDelivery> createSharedQueueInternal(final String name, short type) {

        IQueue<Long, MessageDelivery> ret;

        switch (type) {
        case QueueDescriptor.PARTITIONED: {
            PartitionedQueue<Long, MessageDelivery> queue = new PartitionedQueue<Long, MessageDelivery>(name) {
                @Override
                public IQueue<Long, MessageDelivery> createPartition(int partitionKey) {
                    IQueue<Long, MessageDelivery> queue = createSharedQueueInternal(name + "$" + partitionKey, USE_PRIORITY_QUEUES ? QueueDescriptor.SHARED_PRIORITY : QueueDescriptor.SHARED);
                    queue.getDescriptor().setPartitionId(partitionKey);
                    queue.getDescriptor().setParent(this.getDescriptor().getQueueName());
                    return queue;
                }

            };
            queue.setPartitionMapper(partitionMapper);

            ret = queue;
            break;
        }
        case QueueDescriptor.SHARED_PRIORITY: {
            PrioritySizeLimiter<MessageDelivery> limiter = new PrioritySizeLimiter<MessageDelivery>(DEFAULT_SHARED_QUEUE_SIZE, DEFAULT_SHARED_QUEUE_RESUME_THRESHOLD, MessageBroker.MAX_PRIORITY);
            limiter.setPriorityMapper(PRIORITY_MAPPER);
            limiter.setSizeMapper(SIZE_MAPPER);
            SharedPriorityQueue<Long, MessageDelivery> queue = new SharedPriorityQueue<Long, MessageDelivery>(name, limiter);
            ret = queue;
            queue.setKeyMapper(KEY_MAPPER);
            queue.setAutoRelease(true);
            break;
        }
        case QueueDescriptor.SHARED: {
            SizeLimiter<MessageDelivery> limiter = new SizeLimiter<MessageDelivery>(DEFAULT_SHARED_QUEUE_SIZE, DEFAULT_SHARED_QUEUE_RESUME_THRESHOLD) {
                @Override
                public int getElementSize(MessageDelivery elem) {
                    return elem.getFlowLimiterSize();
                }
            };

            if (!USE_OLD_QUEUE) {
                SharedQueue<Long, MessageDelivery> sQueue = new SharedQueue<Long, MessageDelivery>(name, limiter);
                sQueue.setKeyMapper(KEY_MAPPER);
                sQueue.setAutoRelease(true);
                ret = sQueue;
            } else {
                SharedQueueOld<Long, MessageDelivery> sQueue = new SharedQueueOld<Long, MessageDelivery>(name, limiter);
                sQueue.setKeyMapper(KEY_MAPPER);
                sQueue.setAutoRelease(true);
                ret = sQueue;
            }
            break;
        }
        default: {
            throw new IllegalArgumentException("Unknown queue type" + type);
        }
        }
        ret.getDescriptor().setApplicationType(PARTITION_TYPE);
        ret.setDispatcher(dispatcher);
        ret.setStore(this);
        ret.setPersistencePolicy(SHARED_QUEUE_PERSISTENCE_POLICY);
        ret.setExpirationMapper(EXPIRATION_MAPPER);

        return ret;
    }

    public final void deleteQueueElement(QueueDescriptor descriptor, MessageDelivery elem) {
        elem.acknowledge(descriptor);
    }

    public final boolean isFromStore(MessageDelivery elem) {
        return elem.isFromStore();
    }

    public final void persistQueueElement(SaveableQueueElement<MessageDelivery> elem, ISourceController<?> controller, boolean delayable) {
        elem.getElement().persist(elem, controller, delayable);
    }

    public final void restoreQueueElements(QueueDescriptor queue, boolean recordsOnly, long firstSequence, long maxSequence, int maxCount,
            org.apache.activemq.broker.db.RestoreListener<MessageDelivery> listener) {
        database.restoreMessages(queue, recordsOnly, firstSequence, maxSequence, maxCount, listener);
    }

    public final void addQueue(QueueDescriptor queue) {
        database.addQueue(queue);
    }

    public final void deleteQueue(QueueDescriptor queue) {
        database.deleteQueue(queue);
    }
}