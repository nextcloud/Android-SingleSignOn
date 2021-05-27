package com.nextcloud.android.sso.helper;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Options;
import okio.Sink;
import okio.Timeout;

/**
 *  Nextcloud SingleSignOn
 *
 *  @author David Luhmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class BufferedSourceSSO implements BufferedSource {

    private final InputStream mInputStream;

    public BufferedSourceSSO(InputStream inputStream) {
        this.mInputStream = inputStream;
    }

    @Override
    public Buffer buffer() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Buffer getBuffer() {
        return buffer();
    }

    @Override
    public boolean exhausted() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void require(long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean request(long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public byte readByte() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public short readShort() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public short readShortLe() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int readInt() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int readIntLe() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readLong() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readLongLe() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readDecimalLong() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readHexadecimalUnsignedLong() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void skip(long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ByteString readByteString() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ByteString readByteString(long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int select(Options options) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public byte[] readByteArray() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public byte[] readByteArray(long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int read(byte[] sink) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void readFully(byte[] sink) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void readFully(Buffer sink, long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readAll(Sink sink) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String readUtf8() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String readUtf8(long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nullable
    @Override
    public String readUtf8Line() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String readUtf8LineStrict() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String readUtf8LineStrict(long limit) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int readUtf8CodePoint() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String readString(Charset charset) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String readString(long byteCount, Charset charset) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(byte b) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(byte b, long fromIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(byte b, long fromIndex, long toIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(ByteString bytes) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOfElement(ByteString targetBytes) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean rangeEquals(long offset, ByteString bytes) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BufferedSource peek() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public InputStream inputStream() {
        return mInputStream;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Timeout timeout() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
