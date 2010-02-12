/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * his work for additional information regarding copyright ownership.
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
package org.apache.activemq.amqp.protocol.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import org.apache.activemq.amqp.protocol.AmqpCommand;
import org.apache.activemq.amqp.protocol.AmqpCommandHandler;
import org.apache.activemq.amqp.protocol.marshaller.AmqpEncodingError;
import org.apache.activemq.amqp.protocol.marshaller.AmqpMarshaller;
import org.apache.activemq.amqp.protocol.marshaller.Encoded;
import org.apache.activemq.amqp.protocol.types.IAmqpList;
import org.apache.activemq.util.buffer.Buffer;

/**
 * Represents a update the transfer-limit for a Link
 * <p>
 * This command updates the transfer-limit for the specified Link.
 * </p>
 */
public interface AmqpFlow extends AmqpList, AmqpCommand {



    /**
     * options map
     */
    public void setOptions(AmqpOptions options);

    /**
     * options map
     */
    public AmqpOptions getOptions();

    /**
     * the Link handle
     * <p>
     * Identifies the Link whose transfer-limit is to be updated.
     * </p>
     * <p>
     * command and subsequently used
     * by endpoints as a shorthand to refer to the Link in all outgoing commands. The two
     * endpoints may potentially use different handles to refer to the same Link. Link handles
     * may be reused once a Link is closed for both send and receive.
     * </p>
     */
    public void setHandle(Long handle);

    /**
     * the Link handle
     * <p>
     * Identifies the Link whose transfer-limit is to be updated.
     * </p>
     * <p>
     * command and subsequently used
     * by endpoints as a shorthand to refer to the Link in all outgoing commands. The two
     * endpoints may potentially use different handles to refer to the same Link. Link handles
     * may be reused once a Link is closed for both send and receive.
     * </p>
     */
    public void setHandle(long handle);

    /**
     * the Link handle
     * <p>
     * Identifies the Link whose transfer-limit is to be updated.
     * </p>
     * <p>
     * command and subsequently used
     * by endpoints as a shorthand to refer to the Link in all outgoing commands. The two
     * endpoints may potentially use different handles to refer to the same Link. Link handles
     * may be reused once a Link is closed for both send and receive.
     * </p>
     */
    public void setHandle(AmqpHandle handle);

    /**
     * the Link handle
     * <p>
     * Identifies the Link whose transfer-limit is to be updated.
     * </p>
     * <p>
     * command and subsequently used
     * by endpoints as a shorthand to refer to the Link in all outgoing commands. The two
     * endpoints may potentially use different handles to refer to the same Link. Link handles
     * may be reused once a Link is closed for both send and receive.
     * </p>
     */
    public AmqpHandle getHandle();

    /**
     * the Link transfer-limit
     * <p>
     * The updated value for the transfer-limit. This is the limit beyond which the sent
     * transfer-count for the Link may not exceed. This is an absolute number and must
     * wraparound and compare according to RFC-1982 serial number arithmetic. If this is not
     * set, there is no limit and transfers may be sent until a limit is imposed.
     * </p>
     * <p>
     * A sequence-no encodes a serial number as defined in RFC-1982. The arithmetic, and
     * operators for these numbers are defined by RFC-1982.
     * </p>
     */
    public void setLimit(Long limit);

    /**
     * the Link transfer-limit
     * <p>
     * The updated value for the transfer-limit. This is the limit beyond which the sent
     * transfer-count for the Link may not exceed. This is an absolute number and must
     * wraparound and compare according to RFC-1982 serial number arithmetic. If this is not
     * set, there is no limit and transfers may be sent until a limit is imposed.
     * </p>
     * <p>
     * A sequence-no encodes a serial number as defined in RFC-1982. The arithmetic, and
     * operators for these numbers are defined by RFC-1982.
     * </p>
     */
    public void setLimit(long limit);

    /**
     * the Link transfer-limit
     * <p>
     * The updated value for the transfer-limit. This is the limit beyond which the sent
     * transfer-count for the Link may not exceed. This is an absolute number and must
     * wraparound and compare according to RFC-1982 serial number arithmetic. If this is not
     * set, there is no limit and transfers may be sent until a limit is imposed.
     * </p>
     * <p>
     * A sequence-no encodes a serial number as defined in RFC-1982. The arithmetic, and
     * operators for these numbers are defined by RFC-1982.
     * </p>
     */
    public void setLimit(AmqpSequenceNo limit);

    /**
     * the Link transfer-limit
     * <p>
     * The updated value for the transfer-limit. This is the limit beyond which the sent
     * transfer-count for the Link may not exceed. This is an absolute number and must
     * wraparound and compare according to RFC-1982 serial number arithmetic. If this is not
     * set, there is no limit and transfers may be sent until a limit is imposed.
     * </p>
     * <p>
     * A sequence-no encodes a serial number as defined in RFC-1982. The arithmetic, and
     * operators for these numbers are defined by RFC-1982.
     * </p>
     */
    public AmqpSequenceNo getLimit();

    public static class AmqpFlowBean implements AmqpFlow{

        private AmqpFlowBuffer buffer;
        private AmqpFlowBean bean = this;
        private AmqpOptions options;
        private AmqpHandle handle;
        private AmqpSequenceNo limit;

        AmqpFlowBean() {
        }

        AmqpFlowBean(IAmqpList value) {

        for(int i = 0; i < value.getListCount(); i++) {
            set(i, value.get(i));
        }
    }

    AmqpFlowBean(AmqpFlow.AmqpFlowBean other) {
        this.bean = other;
    }

    public final AmqpFlowBean copy() {
        return new AmqpFlow.AmqpFlowBean(bean);
    }

    public final void handle(AmqpCommandHandler handler) throws Exception {
        handler.handleFlow(this);
    }

    public final AmqpFlow.AmqpFlowBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
        if(buffer == null) {
            buffer = new AmqpFlowBuffer(marshaller.encode(this));
        }
        return buffer;
    }

    public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
        getBuffer(marshaller).marshal(out, marshaller);
    }


    public final void setOptions(AmqpOptions options) {
        copyCheck();
        bean.options = options;
    }

    public final AmqpOptions getOptions() {
        return bean.options;
    }

    public void setHandle(Long handle) {
        setHandle(TypeFactory.createAmqpHandle(handle));
    }


    public void setHandle(long handle) {
        setHandle(TypeFactory.createAmqpHandle(handle));
    }


    public final void setHandle(AmqpHandle handle) {
        copyCheck();
        bean.handle = handle;
    }

    public final AmqpHandle getHandle() {
        return bean.handle;
    }

    public void setLimit(Long limit) {
        setLimit(TypeFactory.createAmqpSequenceNo(limit));
    }


    public void setLimit(long limit) {
        setLimit(TypeFactory.createAmqpSequenceNo(limit));
    }


    public final void setLimit(AmqpSequenceNo limit) {
        copyCheck();
        bean.limit = limit;
    }

    public final AmqpSequenceNo getLimit() {
        return bean.limit;
    }

    public void set(int index, AmqpType<?, ?> value) {
        switch(index) {
        case 0: {
            setOptions((AmqpOptions) value);
            break;
        }
        case 1: {
            setHandle((AmqpHandle) value);
            break;
        }
        case 2: {
            setLimit((AmqpSequenceNo) value);
            break;
        }
        default : {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        }
    }

    public AmqpType<?, ?> get(int index) {
        switch(index) {
        case 0: {
            return bean.options;
        }
        case 1: {
            return bean.handle;
        }
        case 2: {
            return bean.limit;
        }
        default : {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        }
    }

    public int getListCount() {
        return 3;
    }

    public IAmqpList getValue() {
        return bean;
    }

    public Iterator<AmqpType<?, ?>> iterator() {
        return new AmqpListIterator(bean);
    }


    private final void copyCheck() {
        if(buffer != null) {;
            throw new IllegalStateException("unwriteable");
        }
        if(bean != this) {;
            copy(bean);
        }
    }

    private final void copy(AmqpFlow.AmqpFlowBean other) {
        bean = this;
    }

    public boolean equals(Object o){
        if(this == o) {
            return true;
        }

        if(o == null || !(o instanceof AmqpFlow)) {
            return false;
        }

        return equals((AmqpFlow) o);
    }

    public boolean equals(AmqpFlow b) {

        if(b.getOptions() == null ^ getOptions() == null) {
            return false;
        }
        if(b.getOptions() != null && !b.getOptions().equals(getOptions())){ 
            return false;
        }

        if(b.getHandle() == null ^ getHandle() == null) {
            return false;
        }
        if(b.getHandle() != null && !b.getHandle().equals(getHandle())){ 
            return false;
        }

        if(b.getLimit() == null ^ getLimit() == null) {
            return false;
        }
        if(b.getLimit() != null && !b.getLimit().equals(getLimit())){ 
            return false;
        }
        return true;
    }

    public int hashCode() {
        return AbstractAmqpList.hashCodeFor(this);
    }
}

    public static class AmqpFlowBuffer extends AmqpList.AmqpListBuffer implements AmqpFlow{

        private AmqpFlowBean bean;

        protected AmqpFlowBuffer(Encoded<IAmqpList> encoded) {
            super(encoded);
        }

        public final void setOptions(AmqpOptions options) {
            bean().setOptions(options);
        }

        public final AmqpOptions getOptions() {
            return bean().getOptions();
        }

        public void setHandle(Long handle) {
            bean().setHandle(handle);
        }

        public void setHandle(long handle) {
            bean().setHandle(handle);
        }


        public final void setHandle(AmqpHandle handle) {
            bean().setHandle(handle);
        }

        public final AmqpHandle getHandle() {
            return bean().getHandle();
        }

        public void setLimit(Long limit) {
            bean().setLimit(limit);
        }

        public void setLimit(long limit) {
            bean().setLimit(limit);
        }


        public final void setLimit(AmqpSequenceNo limit) {
            bean().setLimit(limit);
        }

        public final AmqpSequenceNo getLimit() {
            return bean().getLimit();
        }

        public void set(int index, AmqpType<?, ?> value) {
            bean().set(index, value);
        }

        public AmqpType<?, ?> get(int index) {
            return bean().get(index);
        }

        public int getListCount() {
            return bean().getListCount();
        }

        public Iterator<AmqpType<?, ?>> iterator() {
            return bean().iterator();
        }

        public IAmqpList getValue() {
            return bean().getValue();
        }

        public AmqpFlow.AmqpFlowBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            return this;
        }

        protected AmqpFlow bean() {
            if(bean == null) {
                bean = new AmqpFlow.AmqpFlowBean(encoded.getValue());
                bean.buffer = this;
            }
            return bean;
        }

        public final void handle(AmqpCommandHandler handler) throws Exception {
            handler.handleFlow(this);
        }

        public boolean equals(Object o){
            return bean().equals(o);
        }

        public boolean equals(AmqpFlow o){
            return bean().equals(o);
        }

        public int hashCode() {
            return bean().hashCode();
        }

        public static AmqpFlow.AmqpFlowBuffer create(Encoded<IAmqpList> encoded) {
            if(encoded.isNull()) {
                return null;
            }
            return new AmqpFlow.AmqpFlowBuffer(encoded);
        }

        public static AmqpFlow.AmqpFlowBuffer create(DataInput in, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError {
            return create(marshaller.unmarshalAmqpFlow(in));
        }

        public static AmqpFlow.AmqpFlowBuffer create(Buffer buffer, int offset, AmqpMarshaller marshaller) throws AmqpEncodingError {
            return create(marshaller.decodeAmqpFlow(buffer, offset));
        }
    }
}