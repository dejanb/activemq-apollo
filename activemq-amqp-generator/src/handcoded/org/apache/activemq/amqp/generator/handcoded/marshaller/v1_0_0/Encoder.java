/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with his work
 * for additional information regarding copyright ownership. The ASF licenses
 * this file to You under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.activemq.amqp.generator.handcoded.marshaller.v1_0_0;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.activemq.amqp.generator.handcoded.marshaller.AmqpVersion;
import org.apache.activemq.amqp.generator.handcoded.marshaller.Encoded;
import org.apache.activemq.amqp.generator.handcoded.marshaller.AmqpEncodingError;
import org.apache.activemq.amqp.generator.handcoded.marshaller.v1_0_0.AmqpMarshaller;
import org.apache.activemq.amqp.generator.handcoded.marshaller.v1_0_0.AmqpListMarshaller.LIST_ENCODING;
import org.apache.activemq.amqp.generator.handcoded.marshaller.v1_0_0.AmqpBinaryMarshaller.BINARY_ENCODING;
import org.apache.activemq.amqp.generator.handcoded.marshaller.v1_0_0.AmqpBooleanMarshaller.BOOLEAN_ENCODING;
import org.apache.activemq.amqp.generator.handcoded.marshaller.v1_0_0.AmqpMapMarshaller.MAP_ENCODING;
import org.apache.activemq.amqp.generator.handcoded.BitUtils;
import org.apache.activemq.amqp.generator.handcoded.types.AmqpType;
import org.apache.activemq.amqp.generator.handcoded.types.IAmqpList;
import org.apache.activemq.amqp.protocol.types.IAmqpMap;
import org.apache.activemq.util.buffer.Buffer;

public class Encoder extends BaseEncoder {

    static final Encoder SINGLETON = new Encoder();
    private static final AmqpMarshaller MARSHALLER = AmqpMarshaller.getMarshaller();

    static final byte NULL_FORMAT_CODE = AmqpNullMarshaller.FORMAT_CODE;
    static final byte DESCRIBED_FORMAT_CODE = (byte) 0x00;

    static final ListDecoder DEFAULT_LIST_DECODER = new ListDecoder() {

        public final AmqpType<?, ?> unmarshalType(int pos, DataInput in) throws IOException, AmqpEncodingError {
            return MARSHALLER.unmarshalType(in);
        }

        public final AmqpType<?, ?> decodeType(int pos, EncodedBuffer buffer) throws AmqpEncodingError {
            return MARSHALLER.decodeType(buffer);
        }
    };

    static final MapDecoder<AmqpType<?,?>, AmqpType<?, ?>> DEFAULT_MAP_DECODER = new MapDecoder<AmqpType<?,?>, AmqpType<?, ?>>() {

        public IAmqpMap<AmqpType<?, ?>, AmqpType<?, ?>> createMap(int entryCount) {
            return new IAmqpMap.AmqpWrapperMap<AmqpType<?,?>, AmqpType<?,?>>(new HashMap<AmqpType<?,?>, AmqpType<?,?>>());
        }

        public void decodeToMap(EncodedBuffer key, EncodedBuffer val, IAmqpMap<AmqpType<?, ?>, AmqpType<?, ?>> map) throws AmqpEncodingError {
            map.put(MARSHALLER.decodeType(key), MARSHALLER.decodeType(key));
            
        }

        public void unmarshalToMap(DataInput in, IAmqpMap<AmqpType<?, ?>, AmqpType<?, ?>> map) throws IOException, AmqpEncodingError {
            map.put(MARSHALLER.unmarshalType(in), MARSHALLER.unmarshalType(in));
        }
    };

    private Encoder() {

    }

    public static enum FormatCategory {
        DESCRIBED(false, false), FIXED(false, false), VARIABLE(true, false), COMPOUND(true, true), ARRAY(true, true);

        private final boolean encodesSize;
        private final boolean encodesCount;

        FormatCategory(boolean encodesSize, boolean encodesCount) {
            this.encodesSize = encodesSize;
            this.encodesCount = encodesCount;
        }

        public static FormatCategory getCategory(byte formatCode) throws IllegalArgumentException {
            switch ((byte) (formatCode & 0xF0)) {
            case (byte) 0x00:
                return DESCRIBED;
            case (byte) 0x40:
            case (byte) 0x50:
            case (byte) 0x60:
            case (byte) 0x70:
            case (byte) 0x80:
            case (byte) 0x90:
                return FIXED;
            case (byte) 0xA0:
            case (byte) 0xB0:
                return VARIABLE;
            case (byte) 0xC0:
            case (byte) 0xD0:
                return COMPOUND;
            case (byte) 0xE0:
            case (byte) 0xF0:
                return ARRAY;
            default:
                throw new IllegalArgumentException("" + formatCode);
            }
        }

        public final boolean encodesSize() {
            return encodesSize;
        }

        public final boolean encodesCount() {
            return encodesCount;
        }

        public static final EncodedBuffer createBuffer(byte formatCode, DataInput in) throws IOException, AmqpEncodingError {
            switch ((byte) (formatCode & 0xF0)) {
            case (byte) 0x00:
                return new DescribedBuffer(formatCode, in);
            case (byte) 0x40:
            case (byte) 0x50:
            case (byte) 0x60:
            case (byte) 0x70:
            case (byte) 0x80:
            case (byte) 0x90:
                return new FixedBuffer(formatCode, in);
            case (byte) 0xA0:
            case (byte) 0xB0:
                return new VariableBuffer(formatCode, in);
            case (byte) 0xC0:
            case (byte) 0xD0:
                return new CompoundBuffer(formatCode, in);
            case (byte) 0xE0:
            case (byte) 0xF0:
                return new ArrayBuffer(formatCode, in);
            default:
                throw new AmqpEncodingError("Invalid format code: " + formatCode);
            }
        }

        public static final EncodedBuffer createBuffer(AbstractEncoded<?> encoded) throws AmqpEncodingError {
            switch ((byte) (encoded.getEncodingFormatCode() & 0xF0)) {
            case (byte) 0x00:
                return new DescribedBuffer(encoded);
            case (byte) 0x40:
            case (byte) 0x50:
            case (byte) 0x60:
            case (byte) 0x70:
            case (byte) 0x80:
            case (byte) 0x90:
                return new FixedBuffer(encoded);
            case (byte) 0xA0:
            case (byte) 0xB0:
                return new VariableBuffer(encoded);
            case (byte) 0xC0:
            case (byte) 0xD0:
                return new CompoundBuffer(encoded);
            case (byte) 0xE0:
            case (byte) 0xF0:
                return new ArrayBuffer(encoded);
            default:
                throw new AmqpEncodingError("Invalid format code: " + encoded.getEncodingFormatCode());
            }
        }

        public static EncodedBuffer createBuffer(Buffer source, int offset) throws AmqpEncodingError {
            byte formatCode = source.get(offset);
            switch ((byte) (formatCode & 0xF0)) {
            case (byte) 0x00:
                return new DescribedBuffer(source, offset);
            case (byte) 0x40:
            case (byte) 0x50:
            case (byte) 0x60:
            case (byte) 0x70:
            case (byte) 0x80:
            case (byte) 0x90:
                return new FixedBuffer(source, offset);
            case (byte) 0xA0:
            case (byte) 0xB0:
                return new VariableBuffer(source, offset);
            case (byte) 0xC0:
            case (byte) 0xD0:
                return new CompoundBuffer(source, offset);
            case (byte) 0xE0:
            case (byte) 0xF0:
                return new ArrayBuffer(source, offset);
            default:
                throw new AmqpEncodingError("Invalid format code: " + formatCode);
            }
        }
    }

    public static enum FormatSubCategory {
        DESCRIBED((byte) 0x00, 0), FIXED_0((byte) 0x40, 0), FIXED_1((byte) 0x50, 1), FIXED_2((byte) 0x60, 2), FIXED_4((byte) 0x70, 4), FIXED_8((byte) 0x80, 8), FIXED_16((byte) 0x90, 16), VARIABLE_1(
                (byte) 0xA0, 1), VARIABLE_4((byte) 0xB0, 4), COMPOUND_1((byte) 0xC0, 1), COMPOUND_4((byte) 0xD0, 4), ARRAY_1((byte) 0xE0, 1), ARRAY_4((byte) 0xF0, 4);

        private final FormatCategory category;
        private final byte subCategory;
        public final int WIDTH;

        FormatSubCategory(byte subCategory, int width) {
            this.subCategory = subCategory;
            category = FormatCategory.getCategory(this.subCategory);
            this.WIDTH = width;

        }

        public static FormatSubCategory getCategory(byte formatCode) throws IllegalArgumentException {
            switch ((byte) (formatCode & 0xF0)) {
            case (byte) 0x00:
                return DESCRIBED;
            case (byte) 0x40:
                return FIXED_0;
            case (byte) 0x50:
                return FIXED_1;
            case (byte) 0x60:
                return FIXED_2;
            case (byte) 0x70:
                return FIXED_4;
            case (byte) 0x80:
                return FIXED_8;
            case (byte) 0x90:
                return FIXED_16;
            case (byte) 0xA0:
                return VARIABLE_1;
            case (byte) 0xB0:
                return VARIABLE_4;
            case (byte) 0xC0:
                return COMPOUND_1;
            case (byte) 0xD0:
                return COMPOUND_4;
            case (byte) 0xE0:
                return ARRAY_1;
            case (byte) 0xF0:
                return ARRAY_4;
            default:
                throw new IllegalArgumentException("" + formatCode);
            }
        }

        public final boolean encodesSize() {
            return category.encodesSize();
        }

        public final boolean encodesCount() {
            return category.encodesCount();
        }

        public final int getEncodedSize(AbstractEncoded<?> encoded) throws AmqpEncodingError {
            if (encoded.getValue() == null) {
                return 1;
            }
            switch (category) {
            case FIXED: {
                return 1 + WIDTH;
            }
            case VARIABLE:
            case COMPOUND: {
                return getDataOffset() + encoded.getDataSize();
            }
            case ARRAY: {
                throw new UnsupportedOperationException("Not implemented");
            }
            case DESCRIBED: {
                throw new UnsupportedOperationException("Not implemented");
            }
            default: {
                throw new IllegalArgumentException(category.name());
            }
            }
        }

        public final int getDataOffset() {
            switch (category) {
            case FIXED: {
                return 1;
            }
            case VARIABLE: {
                return 1 + WIDTH;
            }
            case COMPOUND: {
                return 1 + 2 * WIDTH;
            }
            case ARRAY: {
                throw new UnsupportedOperationException("Not implemented");
            }
            case DESCRIBED: {
                throw new UnsupportedOperationException("Not implemented");
            }
            default: {
                throw new IllegalArgumentException(category.name());
            }
            }
        }

        public void marshalPreData(AbstractEncoded<?> encoded, DataOutput out) throws IOException {
            out.writeByte(encoded.formatCode);

            if (encodesCount()) {
                if (WIDTH == 1) {
                    SINGLETON.writeUbyte((short) encoded.computeDataSize(), out);
                    SINGLETON.writeUbyte((short) encoded.computeDataCount(), out);
                } else {
                    SINGLETON.writeUint((long) encoded.computeDataSize(), out);
                    SINGLETON.writeUbyte((short) encoded.computeDataCount(), out);
                }
            } else if (encodesSize()) {
                if (WIDTH == 1) {
                    SINGLETON.writeUbyte((short) encoded.computeDataSize(), out);
                } else {
                    SINGLETON.writeUint((long) encoded.computeDataSize(), out);
                }
            }
        }

        public final boolean isFixed() {
            return category == FormatCategory.FIXED;
        }
    }

    public static abstract class EncodedBuffer {

        protected final byte formatCode;
        protected final FormatSubCategory category;
        protected Buffer encoded;

        EncodedBuffer(byte formatCode, DataInput in) throws IOException {
            this.formatCode = formatCode;
            this.category = FormatSubCategory.getCategory(formatCode);
            this.encoded = unmarshal(in);
        }

        EncodedBuffer(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            this.formatCode = encodedType.getEncodingFormatCode();
            this.category = FormatSubCategory.getCategory(formatCode);
            this.encoded = fromEncoded(encodedType);
        }

        EncodedBuffer(Buffer source, int offset) throws AmqpEncodingError {
            this.formatCode = source.get(offset);
            this.category = FormatSubCategory.getCategory(formatCode);
            this.encoded = fromBuffer(source, offset);
        }

        public final int getEncodedSize() {
            return encoded.getLength();
        }

        public final Buffer getBuffer() {
            return encoded;
        }

        public final void marshal(DataOutput out) throws IOException {
            out.write(encoded.data, encoded.offset, encoded.length);
        }

        public final byte getEncodingFormatCode() {
            return formatCode;
        }

        public final AmqpVersion getEncodingVersion() {
            return AmqpMarshaller.VERSION;
        }

        protected abstract Buffer fromEncoded(AbstractEncoded<?> encodedType) throws AmqpEncodingError;

        protected abstract Buffer fromBuffer(Buffer buffer, int offset) throws AmqpEncodingError;

        protected abstract Buffer unmarshal(DataInput in) throws IOException;

        public abstract int getConstructorLength();

        public abstract int getDataOffset();

        public abstract int getDataSize() throws AmqpEncodingError;

        public abstract int getDataCount() throws AmqpEncodingError;

        public abstract void marshalConstructor(DataOutput out) throws IOException;

        public abstract void marshalData(DataOutput out) throws IOException;

        public boolean isFixed() {
            return false;
        }

        public FixedBuffer asFixed() {
            throw new AmqpEncodingError(FormatSubCategory.getCategory(formatCode).name());
        }

        public boolean isVariable() {
            return false;
        }

        public VariableBuffer asVariable() {
            throw new AmqpEncodingError(FormatSubCategory.getCategory(formatCode).name());
        }

        public boolean isArray() {
            return false;
        }

        public ArrayBuffer asArray() {
            throw new AmqpEncodingError(FormatSubCategory.getCategory(formatCode).name());
        }

        public boolean isCompound() {
            return false;
        }

        public CompoundBuffer asCompound() {
            throw new AmqpEncodingError(FormatSubCategory.getCategory(formatCode).name());
        }

        public boolean isDescribed() {
            return false;
        }

        public DescribedBuffer asDescribed() {
            throw new AmqpEncodingError(FormatSubCategory.getCategory(formatCode).name());
        }

    }

    public static class FixedBuffer extends EncodedBuffer {

        FixedBuffer(byte formatCode, DataInput in) throws IOException {
            super(formatCode, in);
        }

        FixedBuffer(Buffer source, int offset) throws AmqpEncodingError {
            super(source, offset);
        }

        FixedBuffer(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            super(encodedType);
        }

        public final boolean isFixed() {
            return true;
        }

        public final FixedBuffer asFixed() {
            return this;
        }

        public final int getConstructorLength() {
            return 1;
        }

        public final int getDataOffset() {
            return 1;
        }

        public final int getDataSize() throws AmqpEncodingError {
            return category.WIDTH;
        }

        public final int getDataCount() throws AmqpEncodingError {
            return 1;
        }

        public final void marshalConstructor(DataOutput out) throws IOException {
            out.writeByte(formatCode);
        }

        public final void marshalData(DataOutput out) throws IOException {
            if (getDataSize() > 0) {
                out.write(encoded.data, 1, category.WIDTH);
            }
        }

        protected final Buffer unmarshal(DataInput in) throws IOException {
            Buffer rc = null;
            if (category.WIDTH > 0) {
                rc = new Buffer(1 + category.WIDTH);
                in.readFully(rc.data, 1, category.WIDTH);
            } else {
                rc = new Buffer(1);
            }
            rc.data[0] = formatCode;
            return rc;
        }

        @Override
        protected final Buffer fromBuffer(Buffer source, int offset) {
            Buffer rc = new Buffer(1 + category.WIDTH);
            System.arraycopy(source.data, source.offset + offset, rc.data, 0, rc.length);
            return rc;
        }

        @Override
        protected final Buffer fromEncoded(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            Buffer rc = new Buffer(1 + category.WIDTH);
            rc.data[0] = formatCode;
            encodedType.encode(rc, 1);
            return rc;
        }
    }

    public static class VariableBuffer extends EncodedBuffer {

        int dataSize;

        VariableBuffer(byte formatCode, DataInput in) throws IOException {
            super(formatCode, in);
        }

        VariableBuffer(Buffer source, int offset) throws AmqpEncodingError {
            super(source, offset);
        }

        VariableBuffer(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            super(encodedType);
        }

        public boolean isVariable() {
            return true;
        }

        public VariableBuffer asVariable() {
            return this;
        }

        public int getConstructorLength() {
            return 1;
        }

        public int getDataOffset() {
            return 1 + category.WIDTH;
        }

        public int getDataSize() {
            return dataSize;
        }

        public int getDataCount() {
            return 1;
        }

        public void marshalConstructor(DataOutput out) throws IOException {
            out.writeByte(formatCode);
        }

        public void marshalData(DataOutput out) throws IOException {
            out.write(encoded.data, 1 + encoded.offset, getEncodedSize() - 1);
        }

        @Override
        public Buffer fromEncoded(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            dataSize = encodedType.computeDataSize();
            Buffer rc = new Buffer(1 + category.WIDTH + dataSize);
            rc.data[1] = formatCode;
            if (category.WIDTH == 1) {
                BitUtils.setUByte(rc.data, 1, (short) dataSize);
            } else {
                BitUtils.setUInt(rc.data, 1, dataSize);
            }
            encodedType.encode(rc, getDataOffset());
            return rc;
        }

        @Override
        public Buffer fromBuffer(Buffer source, int offset) {
            offset += source.offset;
            if (category.WIDTH == 1) {
                dataSize = 0xff & source.data[offset + 1];
            } else {
                dataSize = (int) BitUtils.getUInt(source.data, offset + 1);
            }

            Buffer rc = new Buffer(1 + category.WIDTH + dataSize);
            System.arraycopy(source, offset, rc, 0, rc.length);
            return rc;
        }

        public Buffer unmarshal(DataInput in) throws IOException {
            Buffer rc = null;
            byte[] header = new byte[category.WIDTH];
            in.readFully(header);
            if (category.WIDTH == 1) {
                dataSize = 0xff & header[0];
            } else {
                dataSize = (int) BitUtils.getUInt(header, 0);
            }
            rc = new Buffer(1 + header.length + dataSize);
            rc.data[0] = formatCode;
            System.arraycopy(header, 0, rc.data, 1, header.length);
            if (getDataSize() > 0) {
                in.readFully(rc.data, getDataOffset(), dataSize);
            }
            return rc;
        }
    }

    public static class CompoundBuffer extends EncodedBuffer {
        private int dataSize;
        private int dataCount;
        private EncodedBuffer[] constituents;

        CompoundBuffer(byte formatCode, DataInput in) throws IOException {
            super(formatCode, in);
        }

        CompoundBuffer(Buffer source, int offset) throws AmqpEncodingError {
            super(source, offset);
        }

        CompoundBuffer(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            super(encodedType);
        }

        public boolean isCompound() {
            return true;
        }

        public CompoundBuffer asCompound() {
            return this;
        }

        public int getConstructorLength() {
            return 1;
        }

        public int getDataOffset() {
            return 1 + 2 * category.WIDTH;
        }

        public int getDataSize() throws AmqpEncodingError {
            return dataSize;
        }

        public int getDataCount() throws AmqpEncodingError {
            return dataCount;
        }

        public void marshalConstructor(DataOutput out) throws IOException {
            out.writeByte(formatCode);
        }

        public void marshalData(DataOutput out) throws IOException {
            out.write(encoded.data, 1 + encoded.offset, getEncodedSize() - 1);
        }

        public Buffer unmarshal(DataInput in) throws IOException {
            byte[] header = new byte[category.WIDTH * 2];
            in.readFully(header);
            if (category.WIDTH == 1) {
                dataSize = 0xff & header[0];
                dataCount = 0xff & header[1];
            } else {
                dataSize = (int) BitUtils.getUInt(header, 0);
                dataCount = (int) BitUtils.getUInt(header, category.WIDTH);
            }
            Buffer rc = new Buffer(1 + header.length + dataSize);
            rc.data[0] = formatCode;
            System.arraycopy(header, 0, rc.data, 1, header.length);
            if (getDataSize() > 0) {
                in.readFully(rc.data, getDataOffset(), dataSize);
            }
            return rc;
        }

        @Override
        public Buffer fromEncoded(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            dataSize = encodedType.computeDataSize();
            dataCount = encodedType.computeDataCount();

            Buffer rc = new Buffer(1 + category.WIDTH + dataSize);
            rc.data[1] = formatCode;
            if (category.WIDTH == 1) {
                BitUtils.setUByte(rc.data, 1, (short) dataSize);
                BitUtils.setUByte(rc.data, 2, (short) dataSize);
            } else {
                BitUtils.setUInt(rc.data, 1, dataSize);
                BitUtils.setUByte(rc.data, 1 + category.WIDTH, (short) dataSize);
            }
            encodedType.encode(rc, 0);
            return rc;
        }

        @Override
        public Buffer fromBuffer(Buffer buffer, int offset) {
            offset = offset + buffer.offset;
            if (category.WIDTH == 1) {
                dataSize = 0xff & buffer.data[offset + 1];
                dataCount = 0xff & buffer.data[offset + 2];
            } else {
                dataSize = (int) BitUtils.getUInt(buffer.data, offset + 1);
                dataCount = (int) BitUtils.getUInt(buffer.data, offset + 1 + category.WIDTH);
            }
            Buffer rc = new Buffer(1 + category.WIDTH * 2 + dataSize);
            System.arraycopy(buffer.data, offset, rc.data, 0, rc.length);
            return rc;
        }

        EncodedBuffer[] constituents() {
            if (constituents == null) {
                EncodedBuffer[] cb = new EncodedBuffer[getDataCount()];
                Buffer b = getBuffer();
                int offset = getDataOffset();
                for (int i = 0; i < constituents.length; i++) {
                    constituents[i] = FormatCategory.createBuffer(b, offset);
                }
                this.constituents = cb;
            }
            return constituents;
        }
    }

    public static class ArrayBuffer extends EncodedBuffer {
        int dataSize;
        int dataCount;

        ArrayBuffer(byte formatCode, DataInput in) throws IOException {
            super(formatCode, in);
        }

        ArrayBuffer(Buffer source, int offset) throws AmqpEncodingError {
            super(source, offset);
        }

        ArrayBuffer(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            super(encodedType);
        }

        public final boolean isArray() {
            return true;
        }

        public final ArrayBuffer asArray() {
            return this;
        }

        public int getConstructorLength() {
            return 1;
        }

        public int getDataOffset() {
            throw new UnsupportedOperationException();
        }

        public int getDataSize() throws AmqpEncodingError {
            return dataSize;
        }

        public int getDataCount() throws AmqpEncodingError {
            return dataCount;
        }

        public void marshalConstructor(DataOutput out) throws IOException {
            out.write(encoded.data, encoded.offset, getConstructorLength());
        }

        public void marshalData(DataOutput out) throws IOException {
            if (getDataSize() > 0) {
                out.write(encoded.data, encoded.offset + getDataOffset(), getDataSize());
            }
        }

        protected final Buffer unmarshal(DataInput in) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected final Buffer fromEncoded(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            Buffer rc = new Buffer(encodedType.getEncodedSize());
            encodedType.encode(rc, 0);
            return rc;
        }

        @Override
        protected final Buffer fromBuffer(Buffer buffer, int offset) {
            throw new UnsupportedOperationException();
        }
    }

    public static class DescribedBuffer extends EncodedBuffer {

        EncodedBuffer descriptor;
        EncodedBuffer describedBuffer;

        DescribedBuffer(byte formatCode, DataInput in) throws IOException {
            super(formatCode, in);
        }

        DescribedBuffer(Buffer source, int offset) throws AmqpEncodingError {
            super(source, offset);
        }

        DescribedBuffer(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            super(encodedType);
        }

        public final boolean isDescribed() {
            return true;
        }

        public final DescribedBuffer asDescribed() {
            return this;
        }

        public EncodedBuffer getDescriptorBuffer() {
            return descriptor;
        }

        public EncodedBuffer getDescribedBuffer() {
            return describedBuffer;
        }

        public int getConstructorLength() {
            return 1 + descriptor.getEncodedSize() + describedBuffer.getConstructorLength();
        }

        public int getDataOffset() {
            return 1 + descriptor.getEncodedSize() + describedBuffer.getDataOffset();
        }

        public int getDataSize() throws AmqpEncodingError {
            return describedBuffer.getDataSize();
        }

        public int getDataCount() throws AmqpEncodingError {
            return describedBuffer.getDataCount();
        }

        public void marshalConstructor(DataOutput out) throws IOException {
            out.write(encoded.data, 0, getConstructorLength());
        }

        public void marshalData(DataOutput out) throws IOException {
            if (getDataSize() > 0) {
                out.write(encoded.data, getDataOffset(), getDataSize());
            }
        }

        protected final Buffer unmarshal(DataInput in) throws IOException {
            descriptor = FormatCategory.createBuffer(in.readByte(), in);
            describedBuffer = FormatCategory.createBuffer(in.readByte(), in);
            Buffer rc = new Buffer(1 + descriptor.getEncodedSize() + describedBuffer.getEncodedSize());
            rc.data[0] = DESCRIBED_FORMAT_CODE;
            // TODO we should be able to let the described type decode into our
            // buffer
            // which would save a potoentially large copy.
            System.arraycopy(descriptor.getBuffer().data, 0, rc.data, 1, descriptor.getEncodedSize());
            System.arraycopy(describedBuffer.getBuffer().data, 0, rc.data, 1 + descriptor.getEncodedSize(), describedBuffer.getEncodedSize());
            descriptor.encoded = new Buffer(rc.data, 1, descriptor.encoded.getLength());
            describedBuffer.encoded = new Buffer(rc.data, 1 + descriptor.encoded.getLength(), describedBuffer.encoded.getLength());
            return rc;
        }

        @Override
        protected final Buffer fromEncoded(AbstractEncoded<?> encodedType) throws AmqpEncodingError {
            throw new UnsupportedOperationException();
        }

        @Override
        protected final Buffer fromBuffer(Buffer buffer, int offset) throws AmqpEncodingError {
            descriptor = FormatCategory.createBuffer(buffer, offset + 1);
            describedBuffer = FormatCategory.createBuffer(buffer, offset + 1 + descriptor.getEncodedSize());
            Buffer rc = new Buffer(1 + descriptor.getEncodedSize() + describedBuffer.getEncodedSize());
            System.arraycopy(buffer, offset + buffer.offset, rc, 0, rc.length);
            descriptor.encoded = new Buffer(encoded.data, 1, descriptor.encoded.getLength());
            describedBuffer.encoded = new Buffer(encoded.data, 1 + descriptor.encoded.getLength(), describedBuffer.encoded.getLength());
            return rc;
        }
    }

    static abstract class AbstractEncoded<V> implements Encoded<V> {
        private EncodedBuffer encoded;
        private byte formatCode;
        private FormatSubCategory category;
        protected V value;

        // TODO make configurable.
        //private boolean cacheEncoded = true;

        AbstractEncoded(EncodedBuffer encoded) {
            this.encoded = encoded;
            this.formatCode = encoded.formatCode;
            this.category = encoded.category;
        }

        AbstractEncoded(byte formatCode, V value) throws AmqpEncodingError {
            this.value = value;
            this.formatCode = formatCode;
            this.category = FormatSubCategory.getCategory(formatCode);
        }

        public final AmqpVersion getEncodingVersion() {
            return AmqpMarshaller.VERSION;
        }

        public final byte getEncodingFormatCode() {
            return formatCode;
        }

        public boolean isNull() {
            return formatCode == NULL_FORMAT_CODE;
        }

        public final Buffer getBuffer() throws AmqpEncodingError {
            if (encoded == null) {
                encoded = FormatCategory.createBuffer(this);
            }
            return encoded.getBuffer();
        }

        public final V getValue() throws AmqpEncodingError {
            if (value != null || formatCode == AmqpNullMarshaller.FORMAT_CODE) {
                return value;
            }

            value = decode(encoded);
            return value;
        }

        public final int getEncodedSize() throws AmqpEncodingError {
            if (encoded == null) {
                return 1 + getDataSize();
            } else {
                return encoded.getEncodedSize();
            }
        }

        public final int getDataSize() throws AmqpEncodingError {
            if (encoded != null) {
                return encoded.getDataSize();
            } else {
                switch (category.category) {
                case FIXED: {
                    return category.WIDTH;
                }
                case COMPOUND: {
                    return computeDataSize() + category.WIDTH * 2;
                }
                case VARIABLE: {
                    return computeDataSize() + category.WIDTH;
                }
                default: {
                    return computeDataSize();
                }
                }
            }
        }

        public final int getDataCount() throws AmqpEncodingError {
            if (encoded != null) {
                return encoded.getDataCount();
            } else {
                return computeDataCount();
            }
        }

        public final void marshal(DataOutput out) throws IOException {
            if (encoded == null) {
                marshalConstructor(out);
                marshalData(out);
            } else {
                encoded.marshal(out);
            }
        }

        public final void unmarshal(DataInput in) throws IOException {
            throw new UnsupportedOperationException();
        }

        public final void marshalConstructor(DataOutput out) throws IOException {
            if (encoded == null) {
                category.marshalPreData(this, out);
            } else {
                encoded.marshalConstructor(out);
            }
        }

        /**
         * Must be implemented by subclasses that have non fixed width encodings
         * to determine the size of encoded data.
         * 
         * @return The size of the encoded data.
         */
        protected int computeDataSize() throws AmqpEncodingError {
            throw new IllegalStateException("unimplmented");
        }

        /**
         * Must be implemented by subclasses that have compound or array
         * encoding to determine the number of elements that are to be encoded.
         * 
         * @return The number of encoded elements
         */
        protected int computeDataCount() throws AmqpEncodingError {
            throw new IllegalStateException("unimplmented");
        }

        public final void encode(Buffer encoded, int offset) throws AmqpEncodingError {
            encode(value, encoded, offset);
        }

        public abstract void encode(V decoded, Buffer encoded, int offset) throws AmqpEncodingError;

        public abstract V decode(EncodedBuffer buffer) throws AmqpEncodingError;

        abstract V unmarshalData(DataInput in) throws IOException;

        public abstract void marshalData(DataOutput out) throws IOException;
    }

    static class NullEncoded<V> extends AbstractEncoded<V> {

        private static FixedBuffer nb = new FixedBuffer(new Buffer(new byte [] {NULL_FORMAT_CODE}), 0);

        NullEncoded() {
            super(nb);
        }

        @Override
        public V decode(EncodedBuffer buffer) throws AmqpEncodingError {
            return null;
        }

        @Override
        public void encode(V decoded, Buffer encoded, int offset) throws AmqpEncodingError {
        }

        @Override
        public void marshalData(DataOutput out) throws IOException {
        }

        @Override
        V unmarshalData(DataInput in) throws IOException {
            return null;
        }
    }

    public static abstract class DescribedEncoded<V> extends AbstractEncoded<V> {

        Encoded<V> describedEncoded;
        EncodedBuffer descriptor;

        DescribedEncoded(DescribedBuffer encoded) {
            super(encoded);
        }

        DescribedEncoded(Encoded<V> value) {
            super((byte) 0x00, value.getValue());
            describedEncoded = value;
            descriptor = getDescriptor();
        }

        public final void encode(V decoded, Buffer encoded, int offset) throws AmqpEncodingError {
            System.arraycopy(descriptor.encoded, descriptor.encoded.offset, encoded.data, encoded.offset + offset, descriptor.encoded.length);
            describedEncoded.encode(encoded, offset + descriptor.encoded.length);
        }

        public final V decode(EncodedBuffer buffer) throws AmqpEncodingError {
            // TODO remove cast?
            describedEncoded = decodeDescribed(((DescribedBuffer) buffer).describedBuffer);
            return describedEncoded.getValue();
        }

        public final V unmarshalData(DataInput in) throws IOException {
            describedEncoded = unmarshalDescribed(in);
            return describedEncoded.getValue();
        }

        public final void marshalData(DataOutput out) throws IOException {
            descriptor.marshal(out);
            describedEncoded.marshal(out);
        }

        /**
         * Must be implemented by subclasses that have non fixed width encodings
         * to determine the size of encoded data.
         * 
         * @return The size of the encoded data.
         */
        protected final int computeDataSize() throws AmqpEncodingError {
            return descriptor.getEncodedSize() + describedEncoded.getEncodedSize();
        }

        /**
         * Must be implemented by subclasses that have compound or array
         * encoding to determine the number of elements that are to be encoded.
         * 
         * @return The number of encoded elements
         */
        protected final int computeDataCount() throws AmqpEncodingError {
            return 1;
        }

        protected abstract EncodedBuffer getDescriptor();

        protected abstract Encoded<V> decodeDescribed(EncodedBuffer encoded) throws AmqpEncodingError;

        protected abstract Encoded<V> unmarshalDescribed(DataInput in) throws IOException, AmqpEncodingError;
    }

    public static interface ListDecoder {
        AmqpType<?, ?> decodeType(int pos, EncodedBuffer buffer) throws AmqpEncodingError;

        AmqpType<?, ?> unmarshalType(int pos, DataInput in) throws IOException, AmqpEncodingError;
    }

    public static interface MapDecoder<K extends AmqpType<?, ?>, V extends AmqpType<?, ?>> {
        void decodeToMap(EncodedBuffer key, EncodedBuffer val, IAmqpMap<K, V> map) throws AmqpEncodingError;

        public void unmarshalToMap(DataInput in, IAmqpMap<K, V> map) throws IOException, AmqpEncodingError;

        IAmqpMap<K, V> createMap(int entryCount);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Encoding Helpers:
    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    public static final AmqpType<?, ?> decode(Buffer source) throws AmqpEncodingError {
        EncodedBuffer buffer = FormatCategory.createBuffer(source, 0);
        return MARSHALLER.decodeType(buffer);
    }

    public static final AmqpType<?, ?> unmarshalType(DataInput in) throws IOException, AmqpEncodingError {
        return MARSHALLER.decodeType(FormatCategory.createBuffer(in.readByte(), in));
    }

    public final Boolean valueOfBoolean(AmqpBooleanMarshaller.BOOLEAN_ENCODING encoding) {
        return encoding == AmqpBooleanMarshaller.BOOLEAN_ENCODING.TRUE;
    }

    public final Boolean valueOfNull() {
        return null;
    }

    public static final AmqpBinaryMarshaller.BINARY_ENCODING chooseBinaryEncoding(Buffer val) throws AmqpEncodingError {
        if (val.length > 255) {
            return AmqpBinaryMarshaller.BINARY_ENCODING.VBIN32;
        }
        return AmqpBinaryMarshaller.BINARY_ENCODING.VBIN8;
    }

    public static final AmqpBooleanMarshaller.BOOLEAN_ENCODING chooseBooleanEncoding(boolean val) throws AmqpEncodingError {
        if (val) {
            return AmqpBooleanMarshaller.BOOLEAN_ENCODING.TRUE;
        }
        return AmqpBooleanMarshaller.BOOLEAN_ENCODING.FALSE;
    }

    public static final AmqpStringMarshaller.STRING_ENCODING chooseStringEncoding(String val) throws AmqpEncodingError {
        try {
            if (val.length() > 255 || val.getBytes("utf-16").length > 255) {
                return AmqpStringMarshaller.STRING_ENCODING.STR32_UTF16;
            }
        } catch (UnsupportedEncodingException uee) {
            throw new AmqpEncodingError(uee.getMessage(), uee);
        }

        return AmqpStringMarshaller.STRING_ENCODING.STR8_UTF16;
    }

    public static final AmqpSymbolMarshaller.SYMBOL_ENCODING chooseSymbolEncoding(String val) throws AmqpEncodingError {
        try {
            if (val.length() > 255 || val.getBytes("ascii").length > 255) {
                return AmqpSymbolMarshaller.SYMBOL_ENCODING.SYM32;
            }
        } catch (UnsupportedEncodingException uee) {
            throw new AmqpEncodingError(uee.getMessage(), uee);
        }
        return AmqpSymbolMarshaller.SYMBOL_ENCODING.SYM8;
    }

    public final int getEncodedSizeOfBinary(Buffer val, BINARY_ENCODING encoding) {
        return val.length;
    }

    public final int getEncodedSizeOfBoolean(boolean val, BOOLEAN_ENCODING encoding) {
        return 0;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // LIST ENCODINGS
    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    private static class ArrayBackedList implements IAmqpList {
        AmqpType<?, ?>[] list;

        ArrayBackedList(int size) {
            list = new AmqpType<?, ?>[size];
        }

        public AmqpType<?, ?> get(int index) {
            return list[index];
        }

        public int getListCount() {
            return list.length;
        }

        public void set(int index, AmqpType<?, ?> value) {
            list[index] = value;
        }

        public Iterator<AmqpType<?, ?>> iterator() {
            return new AmqpListIterator(this);
        }
    }
    
    public static final AmqpListMarshaller.LIST_ENCODING chooseListEncoding(IAmqpList val) throws AmqpEncodingError {
        if (val.getListCount() > 255) {
            return AmqpListMarshaller.LIST_ENCODING.LIST32;
        }
        int size = 1;
        for (AmqpType<?, ?> le : val) {
            if (le == null) {
                size += 1;
            } else {
                size += le.getBuffer(MARSHALLER).getEncoded().getEncodedSize();
            }
            if (size > 255) {
                return AmqpListMarshaller.LIST_ENCODING.LIST32;
            }
        }
        return AmqpListMarshaller.LIST_ENCODING.LIST8;
    }

    public final int getEncodedSizeOfList(IAmqpList val, LIST_ENCODING encoding) throws AmqpEncodingError {
        int size = 0;
        switch (encoding) {
        // TODO for arrays we need to choose an encoding that is compatible for
        // all
        // values.
        case ARRAY32:
        case ARRAY8: {
            throw new UnsupportedOperationException();
        }
        case LIST32:
        case LIST8: {
            for (AmqpType<?, ?> le : val) {
                if (le == null) {
                    size += 1;
                } else {
                    size += le.getBuffer(MARSHALLER).getEncoded().getEncodedSize();
                }
            }
            return size;
        }
        default: {
            throw new IllegalArgumentException(encoding.name());
        }
        }
    }

    public final int getEncodedCountOfList(IAmqpList val, LIST_ENCODING listENCODING) throws AmqpEncodingError {
        return val.getListCount();
    }

    // List 8 encoding
    public void encodeListList8(IAmqpList value, Buffer encoded, int offset) throws AmqpEncodingError {
        encodeList(value, encoded, offset);
    }

    public IAmqpList decodeListList8(Buffer encoded, int offset, int count, int size, ListDecoder decoder) throws AmqpEncodingError {
        return decodeList(encoded, offset, count, size, decoder);
    }

    public void writeListList8(IAmqpList val, DataOutput out) throws IOException, AmqpEncodingError {
        writeList(val, out);
    }

    public IAmqpList readListList8(int count, int size, DataInput in, ListDecoder decoder) throws IOException, AmqpEncodingError {
        return readList(count, size, in, decoder);
    }

    // List 32 encoding:
    public void encodeListList32(IAmqpList value, Buffer encoded, int offset) throws AmqpEncodingError {
        encodeList(value, encoded, offset);
    }

    public IAmqpList decodeListList32(Buffer encoded, int offset, int count, int size, ListDecoder decoder) throws AmqpEncodingError {
        return decodeList(encoded, offset, count, size, decoder);
    }

    public void writeListList32(IAmqpList val, DataOutput out) throws IOException, AmqpEncodingError {
        writeList(val, out);
    }

    public IAmqpList readListList32(int count, int size, DataInput in, ListDecoder decoder) throws IOException, AmqpEncodingError {
        return readList(count, size, in, decoder);
    }

    // Array 8 encoding
    public void encodeListArray8(IAmqpList value, Buffer encoded, int offset) throws AmqpEncodingError {
        encodeArray(value, encoded, offset);
    }

    public IAmqpList decodeListArray8(Buffer encoded, int offset, int count, int size, ListDecoder decoder) throws AmqpEncodingError {
        return decodeArray(encoded, offset, count, size, decoder);
    }

    public void writeListArray8(IAmqpList val, DataOutput out) throws IOException, AmqpEncodingError {
        writeArray(val, out);
    }

    public IAmqpList readListArray8(int count, int size, DataInput in, ListDecoder decoder) throws IOException, AmqpEncodingError {
        return readArray(count, size, in, decoder);
    }

    // List 32 encoding:
    public void encodeListArray32(IAmqpList value, Buffer encoded, int offset) throws AmqpEncodingError {
        encodeArray(value, encoded, offset);
    }

    public IAmqpList decodeListArray32(Buffer encoded, int offset, int count, int size, ListDecoder decoder) throws AmqpEncodingError {
        return decodeArray(encoded, offset, count, size, decoder);
    }

    public void writeListArray32(IAmqpList val, DataOutput out) throws IOException, AmqpEncodingError {
        writeArray(val, out);
    }

    public IAmqpList readListArray32(int count, int size, DataInput in, ListDecoder decoder) throws IOException, AmqpEncodingError {
        return readArray(count, size, in, decoder);
    }

    // Generic versions:
    public static final void encodeList(IAmqpList value, Buffer target, int offset) throws AmqpEncodingError {
        for (AmqpType<?, ?> le : value) {
            Encoded<?> encoded = le.getBuffer(MARSHALLER).getEncoded();
            encoded.encode(target, offset);
            offset = encoded.getDataSize();
        }
    }

    public static final IAmqpList decodeList(Buffer source, int offset, int count, int size, ListDecoder decoder) throws AmqpEncodingError {
        IAmqpList rc = new ArrayBackedList(count);
        for (int i = 0; i < count; i++) {
            EncodedBuffer encoded = FormatCategory.createBuffer(source, offset);
            offset += encoded.getEncodedSize();
            rc.set(i, decoder.decodeType(i, encoded));
        }
        return rc;
    }

    public static final IAmqpList readList(int count, int size, DataInput in, ListDecoder decoder) throws IOException, AmqpEncodingError {
        IAmqpList rc = new ArrayBackedList(count);
        for (int i = 0; i < count; i++) {
            rc.set(i, decoder.unmarshalType(i, in));
        }
        return rc;
    }

    public static final void writeList(IAmqpList val, DataOutput out) throws IOException, AmqpEncodingError {
        for (AmqpType<?, ?> le : val) {
            if (le == null) {
                out.writeByte(NULL_FORMAT_CODE);
            } else {
                le.marshal(out, MARSHALLER);
            }
        }
    }

    public static final void encodeArray(IAmqpList value, Buffer target, int offset) throws AmqpEncodingError {
        Encoded<?> first = value.get(0).getBuffer(MARSHALLER).getEncoded();
        first.encode(target, offset);
        offset += first.getEncodedSize();

        // TODO
        throw new UnsupportedOperationException();

        // for (int i = 1; i < value.size(); i++) {
        // value.get(i).getBuffer(MARSHALLER).getEncoded()
        // }
    }

    public static final IAmqpList decodeArray(Buffer encoded, int offset, int count, int size, ListDecoder decoder) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public static final IAmqpList readArray(int count, int size, DataInput dis, ListDecoder decoder) throws IOException, AmqpEncodingError {
        // TODO
        throw new UnsupportedOperationException();
    }

    public static final void writeArray(IAmqpList val, DataOutput out) throws IOException, AmqpEncodingError {
        Encoded<?> first = val.get(0).getBuffer(MARSHALLER).getEncoded();
        first.marshal(out);
        for (int i = 1; i < val.getListCount(); i++) {
            val.get(i).getBuffer(MARSHALLER).getEncoded().marshalData(out);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Map ENCODINGS
    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    public static final AmqpMapMarshaller.MAP_ENCODING chooseMapEncoding(IAmqpMap<?, ?> map) throws AmqpEncodingError {
        for (Map.Entry<? extends AmqpType<?,?>, ? extends AmqpType<?,?>> me : map) {
            
            int size = me.getKey().getBuffer(MARSHALLER).getEncoded().getEncodedSize() + me.getValue().getBuffer(MARSHALLER).getEncoded().getEncodedSize();
            if (size > 255) {
                return AmqpMapMarshaller.MAP_ENCODING.MAP32;
            }
        }
        return AmqpMapMarshaller.MAP_ENCODING.MAP8;
    }

    public final int getEncodedSizeOfMap(IAmqpMap<?, ?> map, MAP_ENCODING encoding) throws AmqpEncodingError {
        int size = 0;
        for (Map.Entry<? extends AmqpType<?,?>, ? extends AmqpType<?,?>> me : map) {
            size += me.getKey().getBuffer(MARSHALLER).getEncoded().getEncodedSize() + me.getValue().getBuffer(MARSHALLER).getEncoded().getEncodedSize();
        }
        return size;
    }

    public final int getEncodedCountOfMap(IAmqpMap<?, ?> map, MAP_ENCODING mapENCODING) throws AmqpEncodingError {
        return map.getEntryCount() * 2;
    }

    public final void encodeMapMap32(IAmqpMap<?, ?> value, Buffer target, int offset) throws AmqpEncodingError {
        encodeMap(value, target, offset);
    }

    public final <K extends AmqpType<?,?>, V extends AmqpType<?,?>> IAmqpMap<K, V> decodeMapMap32(Buffer source, int offset, int count, int size, MapDecoder<K, V> decoder) throws AmqpEncodingError {
        return decodeMap(source, offset, count, size, decoder);
    }

    public final void writeMapMap32(IAmqpMap<?, ?> val, DataOutput out) throws AmqpEncodingError, IOException {
        writeMap(val, out);

    }

    public final <K extends AmqpType<?,?>, V extends AmqpType<?,?>> IAmqpMap<K, V> readMapMap32(int count, int size, DataInput in, MapDecoder<K, V> decoder) throws AmqpEncodingError, IOException {
        return readMap(count, size, in, decoder);
    }

    public final void encodeMapMap8(IAmqpMap<?, ?> value, Buffer target, int offset) throws AmqpEncodingError {
        encodeMap(value, target, offset);
    }

    public final <K extends AmqpType<?,?>, V extends AmqpType<?,?>> IAmqpMap<K, V> decodeMapMap8(Buffer source, int offset, int count, int size, MapDecoder<K, V> decoder) throws AmqpEncodingError {
        return decodeMap(source, offset, count, size, decoder);
    }

    public final void writeMapMap8(IAmqpMap<?, ?> val, DataOutput out) throws AmqpEncodingError, IOException {
        writeMap(val, out);

    }

    public final <K extends AmqpType<?,?>, V extends AmqpType<?,?>> IAmqpMap<K, V> readMapMap8(int count, int size, DataInput in, MapDecoder<K, V> decoder) throws AmqpEncodingError, IOException {
        return readMap(count, size, in, decoder);
    }

    public static final <K extends AmqpType<?,?>, V extends AmqpType<?,?>> IAmqpMap<K, V> decodeMap(Buffer source, int offset, int count, int size, MapDecoder<K, V> decoder) throws AmqpEncodingError {
        IAmqpMap<K, V> rc = decoder.createMap(count / 2);
        for (int i = 0; i < count; i += 2) {
            EncodedBuffer encodedKey = FormatCategory.createBuffer(source, offset);
            offset += encodedKey.getEncodedSize();
            EncodedBuffer encodedVal = FormatCategory.createBuffer(source, offset);
            offset += encodedVal.getEncodedSize();
            decoder.decodeToMap(encodedKey, encodedVal, rc);
        }
        return rc;
    }
    

    public static final <K extends AmqpType<?,?>, V extends AmqpType<?,?>> IAmqpMap<K, V> readMap(int count, int size, DataInput in, MapDecoder<K, V> decoder) throws IOException, AmqpEncodingError {
        IAmqpMap<K, V> rc = decoder.createMap(count / 2);
        for (int i = 0; i < count; i += 2) {
            decoder.unmarshalToMap(in, rc);
        }
        return rc;
    }

    public static final void encodeMap(IAmqpMap<?, ?> value, Buffer target, int offset) throws AmqpEncodingError {
        for (Map.Entry<? extends AmqpType<?,?>, ? extends AmqpType<?,?>> me : value) {
            Encoded<?> eKey = me.getKey().getBuffer(MARSHALLER).getEncoded();
            eKey.encode(target, offset);
            offset += eKey.getEncodedSize();

            Encoded<?> eVal = me.getValue().getBuffer(MARSHALLER).getEncoded();
            eVal.encode(target, offset);
            offset += eVal.getEncodedSize();
        }
    }

    public static final void writeMap(IAmqpMap<?, ?> val, DataOutput out) throws IOException, AmqpEncodingError {
        for (Map.Entry<? extends AmqpType<?,?>, ? extends AmqpType<?,?>> me : val) {
            me.getKey().marshal(out, MARSHALLER);
            me.getValue().marshal(out, MARSHALLER);
        }
    }
}