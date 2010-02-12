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
import org.apache.activemq.amqp.protocol.marshaller.AmqpEncodingError;
import org.apache.activemq.amqp.protocol.marshaller.AmqpMarshaller;
import org.apache.activemq.amqp.protocol.marshaller.Encoded;
import org.apache.activemq.amqp.protocol.types.AmqpBinary;
import org.apache.activemq.amqp.protocol.types.AmqpDeliveryTag;
import org.apache.activemq.util.buffer.Buffer;

public interface AmqpDeliveryTag extends AmqpBinary {


    public static class AmqpDeliveryTagBean implements AmqpDeliveryTag{

        private AmqpDeliveryTagBuffer buffer;
        private AmqpDeliveryTagBean bean = this;
        private Buffer value;

        AmqpDeliveryTagBean(Buffer value) {
            this.value = value;
        }

        AmqpDeliveryTagBean(AmqpDeliveryTag.AmqpDeliveryTagBean other) {
            this.bean = other;
        }

        public final AmqpDeliveryTagBean copy() {
            return bean;
        }

        public final AmqpDeliveryTag.AmqpDeliveryTagBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            if(buffer == null) {
                buffer = new AmqpDeliveryTagBuffer(marshaller.encode(this));
            }
            return buffer;
        }

        public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
            getBuffer(marshaller).marshal(out, marshaller);
        }


        public Buffer getValue() {
            return bean.value;
        }


        public boolean equals(Object o){
            if(this == o) {
                return true;
            }

            if(o == null || !(o instanceof AmqpDeliveryTag)) {
                return false;
            }

            return equals((AmqpDeliveryTag) o);
        }

        public boolean equals(AmqpDeliveryTag b) {
            if(b == null) {
                return false;
            }

            if(b.getValue() == null ^ getValue() == null) {
                return false;
            }

            return b.getValue() == null || b.getValue().equals(getValue());
        }

        public int hashCode() {
            if(getValue() == null) {
                return AmqpDeliveryTag.AmqpDeliveryTagBean.class.hashCode();
            }
            return getValue().hashCode();
        }
    }

    public static class AmqpDeliveryTagBuffer extends AmqpBinary.AmqpBinaryBuffer implements AmqpDeliveryTag{

        private AmqpDeliveryTagBean bean;

        protected AmqpDeliveryTagBuffer() {
            super();
        }

        protected AmqpDeliveryTagBuffer(Encoded<Buffer> encoded) {
            super(encoded);
        }

        public Buffer getValue() {
            return bean().getValue();
        }

        public AmqpDeliveryTag.AmqpDeliveryTagBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            return this;
        }

        protected AmqpDeliveryTag bean() {
            if(bean == null) {
                bean = new AmqpDeliveryTag.AmqpDeliveryTagBean(encoded.getValue());
                bean.buffer = this;
            }
            return bean;
        }

        public boolean equals(Object o){
            return bean().equals(o);
        }

        public boolean equals(AmqpDeliveryTag o){
            return bean().equals(o);
        }

        public int hashCode() {
            return bean().hashCode();
        }

        public static AmqpDeliveryTag.AmqpDeliveryTagBuffer create(Encoded<Buffer> encoded) {
            if(encoded.isNull()) {
                return null;
            }
            return new AmqpDeliveryTag.AmqpDeliveryTagBuffer(encoded);
        }

        public static AmqpDeliveryTag.AmqpDeliveryTagBuffer create(DataInput in, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError {
            return create(marshaller.unmarshalAmqpBinary(in));
        }

        public static AmqpDeliveryTag.AmqpDeliveryTagBuffer create(Buffer buffer, int offset, AmqpMarshaller marshaller) throws AmqpEncodingError {
            return create(marshaller.decodeAmqpBinary(buffer, offset));
        }
    }
}