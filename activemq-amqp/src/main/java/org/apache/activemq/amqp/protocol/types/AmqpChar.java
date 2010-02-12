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
import java.lang.Integer;
import org.apache.activemq.amqp.protocol.marshaller.AmqpEncodingError;
import org.apache.activemq.amqp.protocol.marshaller.AmqpMarshaller;
import org.apache.activemq.amqp.protocol.marshaller.Encoded;
import org.apache.activemq.util.buffer.Buffer;

/**
 * Represents a a single unicode character
 */
public interface AmqpChar extends AmqpType<AmqpChar.AmqpCharBean, AmqpChar.AmqpCharBuffer> {


    public Integer getValue();

    public static class AmqpCharBean implements AmqpChar{

        private AmqpCharBuffer buffer;
        private AmqpCharBean bean = this;
        private Integer value;

        AmqpCharBean(Integer value) {
            this.value = value;
        }

        AmqpCharBean(AmqpChar.AmqpCharBean other) {
            this.bean = other;
        }

        public final AmqpCharBean copy() {
            return bean;
        }

        public final AmqpChar.AmqpCharBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            if(buffer == null) {
                buffer = new AmqpCharBuffer(marshaller.encode(this));
            }
            return buffer;
        }

        public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
            getBuffer(marshaller).marshal(out, marshaller);
        }


        public Integer getValue() {
            return bean.value;
        }


        public boolean equals(Object o){
            if(this == o) {
                return true;
            }

            if(o == null || !(o instanceof AmqpChar)) {
                return false;
            }

            return equals((AmqpChar) o);
        }

        public boolean equals(AmqpChar b) {
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
                return AmqpChar.AmqpCharBean.class.hashCode();
            }
            return getValue().hashCode();
        }
    }

    public static class AmqpCharBuffer implements AmqpChar, AmqpBuffer< Integer> {

        private AmqpCharBean bean;
        protected Encoded<Integer> encoded;

        protected AmqpCharBuffer() {
        }

        protected AmqpCharBuffer(Encoded<Integer> encoded) {
            this.encoded = encoded;
        }

        public final Encoded<Integer> getEncoded() throws AmqpEncodingError{
            return encoded;
        }

        public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
            encoded.marshal(out);
        }

        public Integer getValue() {
            return bean().getValue();
        }

        public AmqpChar.AmqpCharBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            return this;
        }

        protected AmqpChar bean() {
            if(bean == null) {
                bean = new AmqpChar.AmqpCharBean(encoded.getValue());
                bean.buffer = this;
            }
            return bean;
        }

        public boolean equals(Object o){
            return bean().equals(o);
        }

        public boolean equals(AmqpChar o){
            return bean().equals(o);
        }

        public int hashCode() {
            return bean().hashCode();
        }

        public static AmqpChar.AmqpCharBuffer create(Encoded<Integer> encoded) {
            if(encoded.isNull()) {
                return null;
            }
            return new AmqpChar.AmqpCharBuffer(encoded);
        }

        public static AmqpChar.AmqpCharBuffer create(DataInput in, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError {
            return create(marshaller.unmarshalAmqpChar(in));
        }

        public static AmqpChar.AmqpCharBuffer create(Buffer buffer, int offset, AmqpMarshaller marshaller) throws AmqpEncodingError {
            return create(marshaller.decodeAmqpChar(buffer, offset));
        }
    }
}