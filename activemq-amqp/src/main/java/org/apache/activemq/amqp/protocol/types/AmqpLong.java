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
import java.lang.Long;
import org.apache.activemq.amqp.protocol.marshaller.AmqpEncodingError;
import org.apache.activemq.amqp.protocol.marshaller.AmqpMarshaller;
import org.apache.activemq.amqp.protocol.marshaller.Encoded;
import org.apache.activemq.util.buffer.Buffer;

/**
 * Represents a integer in the range -(2^63) to 2^63 - 1
 */
public interface AmqpLong extends AmqpType<AmqpLong.AmqpLongBean, AmqpLong.AmqpLongBuffer> {


    public Long getValue();

    public static class AmqpLongBean implements AmqpLong{

        private AmqpLongBuffer buffer;
        private AmqpLongBean bean = this;
        private Long value;

        AmqpLongBean(Long value) {
            this.value = value;
        }

        AmqpLongBean(AmqpLong.AmqpLongBean other) {
            this.bean = other;
        }

        public final AmqpLongBean copy() {
            return bean;
        }

        public final AmqpLong.AmqpLongBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            if(buffer == null) {
                buffer = new AmqpLongBuffer(marshaller.encode(this));
            }
            return buffer;
        }

        public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
            getBuffer(marshaller).marshal(out, marshaller);
        }


        public Long getValue() {
            return bean.value;
        }


        public boolean equals(Object o){
            if(this == o) {
                return true;
            }

            if(o == null || !(o instanceof AmqpLong)) {
                return false;
            }

            return equals((AmqpLong) o);
        }

        public boolean equals(AmqpLong b) {
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
                return AmqpLong.AmqpLongBean.class.hashCode();
            }
            return getValue().hashCode();
        }
    }

    public static class AmqpLongBuffer implements AmqpLong, AmqpBuffer< Long> {

        private AmqpLongBean bean;
        protected Encoded<Long> encoded;

        protected AmqpLongBuffer() {
        }

        protected AmqpLongBuffer(Encoded<Long> encoded) {
            this.encoded = encoded;
        }

        public final Encoded<Long> getEncoded() throws AmqpEncodingError{
            return encoded;
        }

        public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
            encoded.marshal(out);
        }

        public Long getValue() {
            return bean().getValue();
        }

        public AmqpLong.AmqpLongBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            return this;
        }

        protected AmqpLong bean() {
            if(bean == null) {
                bean = new AmqpLong.AmqpLongBean(encoded.getValue());
                bean.buffer = this;
            }
            return bean;
        }

        public boolean equals(Object o){
            return bean().equals(o);
        }

        public boolean equals(AmqpLong o){
            return bean().equals(o);
        }

        public int hashCode() {
            return bean().hashCode();
        }

        public static AmqpLong.AmqpLongBuffer create(Encoded<Long> encoded) {
            if(encoded.isNull()) {
                return null;
            }
            return new AmqpLong.AmqpLongBuffer(encoded);
        }

        public static AmqpLong.AmqpLongBuffer create(DataInput in, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError {
            return create(marshaller.unmarshalAmqpLong(in));
        }

        public static AmqpLong.AmqpLongBuffer create(Buffer buffer, int offset, AmqpMarshaller marshaller) throws AmqpEncodingError {
            return create(marshaller.decodeAmqpLong(buffer, offset));
        }
    }
}