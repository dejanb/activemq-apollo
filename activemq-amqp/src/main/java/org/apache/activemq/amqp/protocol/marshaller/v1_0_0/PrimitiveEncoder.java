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
package org.apache.activemq.amqp.protocol.marshaller.v1_0_0;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;
import org.apache.activemq.amqp.protocol.marshaller.AmqpEncodingError;
import org.apache.activemq.util.buffer.Buffer;


public interface PrimitiveEncoder {

    /**
     * Writes a Integer encoded as a UTF-32 encoded unicode character
     */
    public void writeChar(Integer val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Integer as a UTF-32 encoded unicode character
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeChar(Integer val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Integer encoded as a UTF-32 encoded unicode character
     */
    public Integer readChar(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Integer encoded as a UTF-32 encoded unicode character
     */
    public Integer decodeChar(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a Long encoded as 64-bit two's-complement integer in network byte order
     */
    public void writeLong(Long val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Long as 64-bit two's-complement integer in network byte order
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeLong(Long val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Long encoded as 64-bit two's-complement integer in network byte order
     */
    public Long readLong(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Long encoded as 64-bit two's-complement integer in network byte order
     */
    public Long decodeLong(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a Float encoded as IEEE 754-2008 binary32
     */
    public void writeFloat(Float val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Float as IEEE 754-2008 binary32
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeFloat(Float val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Float encoded as IEEE 754-2008 binary32
     */
    public Float readFloat(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Float encoded as IEEE 754-2008 binary32
     */
    public Float decodeFloat(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a Byte encoded as 8-bit two's-complement integer
     */
    public void writeByte(Byte val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Byte as 8-bit two's-complement integer
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeByte(Byte val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Byte encoded as 8-bit two's-complement integer
     */
    public Byte readByte(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Byte encoded as 8-bit two's-complement integer
     */
    public Byte decodeByte(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a BigInteger encoded as 64-bit unsigned integer in network byte order
     */
    public void writeUlong(BigInteger val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a BigInteger as 64-bit unsigned integer in network byte order
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeUlong(BigInteger val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a BigInteger encoded as 64-bit unsigned integer in network byte order
     */
    public BigInteger readUlong(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a BigInteger encoded as 64-bit unsigned integer in network byte order
     */
    public BigInteger decodeUlong(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a String encoded as up to 2^8 - 1 seven bit ASCII characters representing a symbolic value
     */
    public void writeSymbolSym8(String val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a String as up to 2^8 - 1 seven bit ASCII characters representing a symbolic value
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeSymbolSym8(String val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a String encoded as up to 2^8 - 1 seven bit ASCII characters representing a symbolic value
     */
    public String readSymbolSym8(int size, DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a String encoded as up to 2^8 - 1 seven bit ASCII characters representing a symbolic value
     */
    public String decodeSymbolSym8(Buffer encoded, int offset, int length) throws AmqpEncodingError;

    /**
     * Writes a String encoded as up to 2^32 - 1 seven bit ASCII characters representing a symbolic value
     */
    public void writeSymbolSym32(String val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a String as up to 2^32 - 1 seven bit ASCII characters representing a symbolic value
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeSymbolSym32(String val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a String encoded as up to 2^32 - 1 seven bit ASCII characters representing a symbolic value
     */
    public String readSymbolSym32(int size, DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a String encoded as up to 2^32 - 1 seven bit ASCII characters representing a symbolic value
     */
    public String decodeSymbolSym32(Buffer encoded, int offset, int length) throws AmqpEncodingError;

    /**
     * Writes a Integer encoded as 32-bit two's-complement integer in network byte order
     */
    public void writeInt(Integer val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Integer as 32-bit two's-complement integer in network byte order
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeInt(Integer val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Integer encoded as 32-bit two's-complement integer in network byte order
     */
    public Integer readInt(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Integer encoded as 32-bit two's-complement integer in network byte order
     */
    public Integer decodeInt(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a Double encoded as IEEE 754-2008 binary64
     */
    public void writeDouble(Double val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Double as IEEE 754-2008 binary64
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeDouble(Double val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Double encoded as IEEE 754-2008 binary64
     */
    public Double readDouble(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Double encoded as IEEE 754-2008 binary64
     */
    public Double decodeDouble(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a UUID encoded as UUID as defined in section 4.1.2 of RFC-4122
     */
    public void writeUuid(UUID val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a UUID as UUID as defined in section 4.1.2 of RFC-4122
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeUuid(UUID val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a UUID encoded as UUID as defined in section 4.1.2 of RFC-4122
     */
    public UUID readUuid(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a UUID encoded as UUID as defined in section 4.1.2 of RFC-4122
     */
    public UUID decodeUuid(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a Buffer encoded as up to 2^8 - 1 octets of binary data
     */
    public void writeBinaryVbin8(Buffer val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Buffer as up to 2^8 - 1 octets of binary data
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeBinaryVbin8(Buffer val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Buffer encoded as up to 2^8 - 1 octets of binary data
     */
    public Buffer readBinaryVbin8(int size, DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Buffer encoded as up to 2^8 - 1 octets of binary data
     */
    public Buffer decodeBinaryVbin8(Buffer encoded, int offset, int length) throws AmqpEncodingError;

    /**
     * Writes a Buffer encoded as up to 2^32 - 1 octets of binary data
     */
    public void writeBinaryVbin32(Buffer val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Buffer as up to 2^32 - 1 octets of binary data
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeBinaryVbin32(Buffer val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Buffer encoded as up to 2^32 - 1 octets of binary data
     */
    public Buffer readBinaryVbin32(int size, DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Buffer encoded as up to 2^32 - 1 octets of binary data
     */
    public Buffer decodeBinaryVbin32(Buffer encoded, int offset, int length) throws AmqpEncodingError;

    /**
     * Writes a Date encoded as 64-bit signed integer representing milliseconds since the unix epoch
     */
    public void writeTimestamp(Date val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Date as 64-bit signed integer representing milliseconds since the unix epoch
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeTimestamp(Date val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Date encoded as 64-bit signed integer representing milliseconds since the unix epoch
     */
    public Date readTimestamp(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Date encoded as 64-bit signed integer representing milliseconds since the unix epoch
     */
    public Date decodeTimestamp(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a Short encoded as 16-bit two's-complement integer in network byte order
     */
    public void writeShort(Short val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Short as 16-bit two's-complement integer in network byte order
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeShort(Short val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Short encoded as 16-bit two's-complement integer in network byte order
     */
    public Short readShort(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Short encoded as 16-bit two's-complement integer in network byte order
     */
    public Short decodeShort(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a Integer encoded as 16-bit unsigned integer in network byte order
     */
    public void writeUshort(Integer val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Integer as 16-bit unsigned integer in network byte order
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeUshort(Integer val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Integer encoded as 16-bit unsigned integer in network byte order
     */
    public Integer readUshort(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Integer encoded as 16-bit unsigned integer in network byte order
     */
    public Integer decodeUshort(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a String encoded as up to 2^8 - 1 octets worth of UTF-8 unicode
     */
    public void writeStringStr8Utf8(String val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a String as up to 2^8 - 1 octets worth of UTF-8 unicode
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeStringStr8Utf8(String val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a String encoded as up to 2^8 - 1 octets worth of UTF-8 unicode
     */
    public String readStringStr8Utf8(int size, DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a String encoded as up to 2^8 - 1 octets worth of UTF-8 unicode
     */
    public String decodeStringStr8Utf8(Buffer encoded, int offset, int length) throws AmqpEncodingError;

    /**
     * Writes a String encoded as up to 2^8 - 1 octets worth of UTF-16 unicode
     */
    public void writeStringStr8Utf16(String val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a String as up to 2^8 - 1 octets worth of UTF-16 unicode
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeStringStr8Utf16(String val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a String encoded as up to 2^8 - 1 octets worth of UTF-16 unicode
     */
    public String readStringStr8Utf16(int size, DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a String encoded as up to 2^8 - 1 octets worth of UTF-16 unicode
     */
    public String decodeStringStr8Utf16(Buffer encoded, int offset, int length) throws AmqpEncodingError;

    /**
     * Writes a String encoded as up to 2^32 - 1 octets worth of UTF-8 unicode
     */
    public void writeStringStr32Utf8(String val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a String as up to 2^32 - 1 octets worth of UTF-8 unicode
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeStringStr32Utf8(String val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a String encoded as up to 2^32 - 1 octets worth of UTF-8 unicode
     */
    public String readStringStr32Utf8(int size, DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a String encoded as up to 2^32 - 1 octets worth of UTF-8 unicode
     */
    public String decodeStringStr32Utf8(Buffer encoded, int offset, int length) throws AmqpEncodingError;

    /**
     * Writes a String encoded as up to 2^32 - 1 octets worth of UTF-16 unicode
     */
    public void writeStringStr32Utf16(String val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a String as up to 2^32 - 1 octets worth of UTF-16 unicode
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeStringStr32Utf16(String val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a String encoded as up to 2^32 - 1 octets worth of UTF-16 unicode
     */
    public String readStringStr32Utf16(int size, DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a String encoded as up to 2^32 - 1 octets worth of UTF-16 unicode
     */
    public String decodeStringStr32Utf16(Buffer encoded, int offset, int length) throws AmqpEncodingError;

    /**
     * Writes a Short encoded as 8-bit unsigned integer
     */
    public void writeUbyte(Short val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Short as 8-bit unsigned integer
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeUbyte(Short val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Short encoded as 8-bit unsigned integer
     */
    public Short readUbyte(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Short encoded as 8-bit unsigned integer
     */
    public Short decodeUbyte(Buffer encoded, int offset) throws AmqpEncodingError;

    /**
     * Writes a Long encoded as 32-bit unsigned integer in network byte order
     */
    public void writeUint(Long val, DataOutput buf) throws IOException, AmqpEncodingError;
    /**
     * Encodes a Long as 32-bit unsigned integer in network byte order
     * 
     * The encoded data should be written into the supplied buffer at the given offset.
     */
    public void encodeUint(Long val, Buffer buf, int offset) throws AmqpEncodingError;

    /**
     * Reads a Long encoded as 32-bit unsigned integer in network byte order
     */
    public Long readUint(DataInput dis) throws IOException, AmqpEncodingError;

    /**
     * Decodes a Long encoded as 32-bit unsigned integer in network byte order
     */
    public Long decodeUint(Buffer encoded, int offset) throws AmqpEncodingError;
}