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
import java.lang.Double;
import org.apache.activemq.amqp.protocol.marshaller.AmqpEncodingError;
import org.apache.activemq.amqp.protocol.marshaller.AmqpMarshaller;
import org.apache.activemq.amqp.protocol.marshaller.Encoded;
import org.apache.activemq.util.buffer.Buffer;

/**
 * Represents a 64-bit floating point number (IEEE 754-2008 binary64)
 */
public interface AmqpDouble extends AmqpType<AmqpDouble.AmqpDoubleBean, AmqpDouble.AmqpDoubleBuffer> {


    public Double getValue();

    public static class AmqpDoubleBean implements AmqpDouble{

        private AmqpDoubleBuffer buffer;
        private AmqpDoubleBean bean = this;
        private Double value;

        AmqpDoubleBean(Double value) {
            this.value = value;
        }

        AmqpDoubleBean(AmqpDouble.AmqpDoubleBean other) {
            this.bean = other;
        }

        public final AmqpDoubleBean copy() {
            return bean;
        }

        public final AmqpDouble.AmqpDoubleBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            if(buffer == null) {
                buffer = new AmqpDoubleBuffer(marshaller.encode(this));
            }
            return buffer;
        }

        public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
            getBuffer(marshaller).marshal(out, marshaller);
        }


        public Double getValue() {
            return bean.value;
        }


        public boolean equals(Object o){
            if(this == o) {
                return true;
            }

            if(o == null || !(o instanceof AmqpDouble)) {
                return false;
            }

            return equals((AmqpDouble) o);
        }

        public boolean equals(AmqpDouble b) {
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
                return AmqpDouble.AmqpDoubleBean.class.hashCode();
            }
            return getValue().hashCode();
        }
    }

    public static class AmqpDoubleBuffer implements AmqpDouble, AmqpBuffer< Double> {

        private AmqpDoubleBean bean;
        protected Encoded<Double> encoded;

        protected AmqpDoubleBuffer() {
        }

        protected AmqpDoubleBuffer(Encoded<Double> encoded) {
            this.encoded = encoded;
        }

        public final Encoded<Double> getEncoded() throws AmqpEncodingError{
            return encoded;
        }

        public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
            encoded.marshal(out);
        }

        public Double getValue() {
            return bean().getValue();
        }

        public AmqpDouble.AmqpDoubleBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            return this;
        }

        protected AmqpDouble bean() {
            if(bean == null) {
                bean = new AmqpDouble.AmqpDoubleBean(encoded.getValue());
                bean.buffer = this;
            }
            return bean;
        }

        public boolean equals(Object o){
            return bean().equals(o);
        }

        public boolean equals(AmqpDouble o){
            return bean().equals(o);
        }

        public int hashCode() {
            return bean().hashCode();
        }

        public static AmqpDouble.AmqpDoubleBuffer create(Encoded<Double> encoded) {
            if(encoded.isNull()) {
                return null;
            }
            return new AmqpDouble.AmqpDoubleBuffer(encoded);
        }

        public static AmqpDouble.AmqpDoubleBuffer create(DataInput in, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError {
            return create(marshaller.unmarshalAmqpDouble(in));
        }

        public static AmqpDouble.AmqpDoubleBuffer create(Buffer buffer, int offset, AmqpMarshaller marshaller) throws AmqpEncodingError {
            return create(marshaller.decodeAmqpDouble(buffer, offset));
        }
    }
}