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
package org.apache.activemq.dispatch;

import java.nio.channels.SelectableChannel;


/**
 * Provides easy access to a system wide Dispatcher.
 * 
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class DispatchSystem {

    final private static Dispatcher dispatcher = create();

    private static Dispatcher create() {
        Dispatcher rc = new DispatcherConfig().createDispatcher();
        rc.retain();
        return rc;
    }
    
    static DispatchQueue getMainQueue() {
        return dispatcher.getMainQueue();
    }
    
    static public DispatchQueue getGlobalQueue() {
        return dispatcher.getGlobalQueue();
    }
    
    static public DispatchQueue getGlobalQueue(DispatchPriority priority) {
        return dispatcher.getGlobalQueue(priority);
    }
    
    static DispatchQueue getSerialQueue(String label, DispatchOption...options) {
        return dispatcher.createSerialQueue(label, options);
    }
    
    static void dispatchMain() {
        dispatcher.dispatchMain();
    }

    static DispatchSource createSource(SelectableChannel channel, int interestOps, DispatchQueue queue) {
        return dispatcher.createSource(channel, interestOps, queue);
    }


}
