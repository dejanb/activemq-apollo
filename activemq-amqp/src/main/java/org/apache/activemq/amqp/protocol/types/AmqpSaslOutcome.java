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
import org.apache.activemq.amqp.protocol.marshaller.AmqpEncodingError;
import org.apache.activemq.amqp.protocol.marshaller.AmqpMarshaller;
import org.apache.activemq.amqp.protocol.marshaller.Encoded;
import org.apache.activemq.amqp.protocol.types.IAmqpList;
import org.apache.activemq.util.buffer.Buffer;

/**
 * Represents a indicates the outcome of the sasl dialog
 * <p>
 * This control indicates the outcome of the SASL dialog. Upon successful completion of the
 * SASL dialog the Security Layer has been established, and the peers must exchange protocol
 * headers to either start a nested Security Layer, or to establish the AMQP Connection.
 * </p>
 */
public interface AmqpSaslOutcome extends AmqpList {



    /**
     * options map
     */
    public void setOptions(AmqpMap options);

    /**
     * options map
     */
    public IAmqpMap<AmqpType<?, ?>, AmqpType<?, ?>> getOptions();

    /**
     * indicates the outcome of the sasl dialog
     * <p>
     * A reply-code indicating the outcome of the SASL dialog.
     * </p>
     */
    public void setCode(AmqpSaslCode code);

    /**
     * indicates the outcome of the sasl dialog
     * <p>
     * A reply-code indicating the outcome of the SASL dialog.
     * </p>
     */
    public AmqpSaslCode getCode();

    /**
     * additional data as specified in RFC-4422
     * <p>
     * The additional-data field carries additional data on successful authentication outcome
     * as specified by the SASL specification (RFC-4422). If the authentication is
     * unsuccessful, this field is not set.
     * </p>
     */
    public void setAdditionalData(Buffer additionalData);

    /**
     * additional data as specified in RFC-4422
     * <p>
     * The additional-data field carries additional data on successful authentication outcome
     * as specified by the SASL specification (RFC-4422). If the authentication is
     * unsuccessful, this field is not set.
     * </p>
     */
    public void setAdditionalData(AmqpBinary additionalData);

    /**
     * additional data as specified in RFC-4422
     * <p>
     * The additional-data field carries additional data on successful authentication outcome
     * as specified by the SASL specification (RFC-4422). If the authentication is
     * unsuccessful, this field is not set.
     * </p>
     */
    public Buffer getAdditionalData();

    public static class AmqpSaslOutcomeBean implements AmqpSaslOutcome{

        private AmqpSaslOutcomeBuffer buffer;
        private AmqpSaslOutcomeBean bean = this;
        private AmqpMap options;
        private AmqpSaslCode code;
        private AmqpBinary additionalData;

        AmqpSaslOutcomeBean() {
        }

        AmqpSaslOutcomeBean(IAmqpList value) {

        for(int i = 0; i < value.getListCount(); i++) {
            set(i, value.get(i));
        }
    }

    AmqpSaslOutcomeBean(AmqpSaslOutcome.AmqpSaslOutcomeBean other) {
        this.bean = other;
    }

    public final AmqpSaslOutcomeBean copy() {
        return new AmqpSaslOutcome.AmqpSaslOutcomeBean(bean);
    }

    public final AmqpSaslOutcome.AmqpSaslOutcomeBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
        if(buffer == null) {
            buffer = new AmqpSaslOutcomeBuffer(marshaller.encode(this));
        }
        return buffer;
    }

    public final void marshal(DataOutput out, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError{
        getBuffer(marshaller).marshal(out, marshaller);
    }


    public final void setOptions(AmqpMap options) {
        copyCheck();
        bean.options = options;
    }

    public final IAmqpMap<AmqpType<?, ?>, AmqpType<?, ?>> getOptions() {
        return bean.options.getValue();
    }

    public final void setCode(AmqpSaslCode code) {
        copyCheck();
        bean.code = code;
    }

    public final AmqpSaslCode getCode() {
        return bean.code;
    }

    public void setAdditionalData(Buffer additionalData) {
        setAdditionalData(TypeFactory.createAmqpBinary(additionalData));
    }


    public final void setAdditionalData(AmqpBinary additionalData) {
        copyCheck();
        bean.additionalData = additionalData;
    }

    public final Buffer getAdditionalData() {
        return bean.additionalData.getValue();
    }

    public void set(int index, AmqpType<?, ?> value) {
        switch(index) {
        case 0: {
            setOptions((AmqpMap) value);
            break;
        }
        case 1: {
            setCode(AmqpSaslCode.get((AmqpUbyte)value));
            break;
        }
        case 2: {
            setAdditionalData((AmqpBinary) value);
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
            if(code == null) {
                return null;
            }
            return code.getValue();
        }
        case 2: {
            return bean.additionalData;
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

    private final void copy(AmqpSaslOutcome.AmqpSaslOutcomeBean other) {
        bean = this;
    }

    public boolean equals(Object o){
        if(this == o) {
            return true;
        }

        if(o == null || !(o instanceof AmqpSaslOutcome)) {
            return false;
        }

        return equals((AmqpSaslOutcome) o);
    }

    public boolean equals(AmqpSaslOutcome b) {

        if(b.getOptions() == null ^ getOptions() == null) {
            return false;
        }
        if(b.getOptions() != null && !b.getOptions().equals(getOptions())){ 
            return false;
        }

        if(b.getCode() == null ^ getCode() == null) {
            return false;
        }
        if(b.getCode() != null && !b.getCode().equals(getCode())){ 
            return false;
        }

        if(b.getAdditionalData() == null ^ getAdditionalData() == null) {
            return false;
        }
        if(b.getAdditionalData() != null && !b.getAdditionalData().equals(getAdditionalData())){ 
            return false;
        }
        return true;
    }

    public int hashCode() {
        return AbstractAmqpList.hashCodeFor(this);
    }
}

    public static class AmqpSaslOutcomeBuffer extends AmqpList.AmqpListBuffer implements AmqpSaslOutcome{

        private AmqpSaslOutcomeBean bean;

        protected AmqpSaslOutcomeBuffer(Encoded<IAmqpList> encoded) {
            super(encoded);
        }

        public final void setOptions(AmqpMap options) {
            bean().setOptions(options);
        }

        public final IAmqpMap<AmqpType<?, ?>, AmqpType<?, ?>> getOptions() {
            return bean().getOptions();
        }

        public final void setCode(AmqpSaslCode code) {
            bean().setCode(code);
        }

        public final AmqpSaslCode getCode() {
            return bean().getCode();
        }

        public void setAdditionalData(Buffer additionalData) {
            bean().setAdditionalData(additionalData);
        }

        public final void setAdditionalData(AmqpBinary additionalData) {
            bean().setAdditionalData(additionalData);
        }

        public final Buffer getAdditionalData() {
            return bean().getAdditionalData();
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

        public AmqpSaslOutcome.AmqpSaslOutcomeBuffer getBuffer(AmqpMarshaller marshaller) throws AmqpEncodingError{
            return this;
        }

        protected AmqpSaslOutcome bean() {
            if(bean == null) {
                bean = new AmqpSaslOutcome.AmqpSaslOutcomeBean(encoded.getValue());
                bean.buffer = this;
            }
            return bean;
        }

        public boolean equals(Object o){
            return bean().equals(o);
        }

        public boolean equals(AmqpSaslOutcome o){
            return bean().equals(o);
        }

        public int hashCode() {
            return bean().hashCode();
        }

        public static AmqpSaslOutcome.AmqpSaslOutcomeBuffer create(Encoded<IAmqpList> encoded) {
            if(encoded.isNull()) {
                return null;
            }
            return new AmqpSaslOutcome.AmqpSaslOutcomeBuffer(encoded);
        }

        public static AmqpSaslOutcome.AmqpSaslOutcomeBuffer create(DataInput in, AmqpMarshaller marshaller) throws IOException, AmqpEncodingError {
            return create(marshaller.unmarshalAmqpSaslOutcome(in));
        }

        public static AmqpSaslOutcome.AmqpSaslOutcomeBuffer create(Buffer buffer, int offset, AmqpMarshaller marshaller) throws AmqpEncodingError {
            return create(marshaller.decodeAmqpSaslOutcome(buffer, offset));
        }
    }
}