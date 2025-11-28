/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2020 Desperate Coder <echotodevnull@gmail.com>
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2018-2019 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Options;
import okio.Sink;
import okio.Timeout;
import okio.TypedOptions;

public class BufferedSourceSSO implements BufferedSource {

    private final InputStream mInputStream;

    public BufferedSourceSSO(InputStream inputStream) {
        this.mInputStream = inputStream;
    }

    @NonNull
    @Override
    public Buffer buffer() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public Buffer getBuffer() {
        return buffer();
    }

    @Override
    public boolean exhausted() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void require(long byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean request(long byteCount) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public byte readByte() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public short readShort() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public short readShortLe() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int readInt() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int readIntLe() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readLong() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readLongLe() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readDecimalLong() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readHexadecimalUnsignedLong() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void skip(long byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public ByteString readByteString() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public ByteString readByteString(long byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int select(@NonNull Options options) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public byte[] readByteArray() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public byte[] readByteArray(long byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int read(byte[] sink) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void readFully(byte[] sink) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void readFully(@NonNull Buffer sink, long byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long readAll(@NonNull Sink sink) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public String readUtf8() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public String readUtf8(long byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nullable
    @Override
    public String readUtf8Line() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public String readUtf8LineStrict() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public String readUtf8LineStrict(long limit) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int readUtf8CodePoint() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public String readString(@NonNull Charset charset) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public String readString(long byteCount, @NonNull Charset charset) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(byte b) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(byte b, long fromIndex) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(byte b, long fromIndex, long toIndex) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(@NonNull ByteString bytes) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOf(@NonNull ByteString bytes, long fromIndex) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOfElement(@NonNull ByteString targetBytes) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long indexOfElement(@NonNull ByteString targetBytes, long fromIndex) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean rangeEquals(long offset, @NonNull ByteString bytes) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean rangeEquals(long offset, @NonNull ByteString bytes, int bytesOffset, int byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public BufferedSource peek() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
    @Override
    public InputStream inputStream() {
        return mInputStream;
    }

    @Override
    public long read(@NonNull Buffer sink, long byteCount) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @NonNull
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
    public int read(ByteBuffer dst) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Nullable
    @Override
    public <T> T select(@NonNull TypedOptions<T> typedOptions) {
        return null;
    }

    @Override
    public long indexOf(@NonNull ByteString byteString, long l, long l1) {
        return 0;
    }
}
