/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.nio.charset.StandardCharsets

class AidlNetworkRequestTest {

    /**
     * Exception class only used to be renamed in the serialized stream, simulating an exception
     * type that exists in the Files app but not in the client app (e.g.
     * `com.owncloud.android.lib.common.network.CertificateCombinedException`).
     */
    class OnlyInFilesAppException(message: String) : Exception(message)

    @Test
    fun deserializeObjectV2ExceptionClassMissingInClientAppReturnsReadableException() {
        val data = serialize(OnlyInFilesAppException("certificate expired"), "[]")

        // Rename the class inside the serialized stream to one that does not exist on this
        // side of the IPC channel (same length, so the stream structure stays intact)
        val tampered = replaceBytes(data, "OnlyInFilesAppException", "OnlyInF1lesAppException")

        val response = AidlNetworkRequest.deserializeObjectV2(ByteArrayInputStream(tampered))

        val exception = response.exception()
        assertNotNull(exception)
        assertFalse(exception is ClassNotFoundException)
        assertNotNull(exception!!.message)
        assertTrue(
            "message should contain the original exception type",
            exception.message!!.contains("OnlyInF1lesAppException")
        )
    }

    @Test
    fun deserializeObjectV2NoExceptionParsesHeaders() {
        val data = serialize(null, "[{\"name\":\"Content-Type\",\"value\":\"application/json\"}]")

        val response = AidlNetworkRequest.deserializeObjectV2(ByteArrayInputStream(data))

        assertNull(response.exception())
        assertEquals(1, response.headers().size)
        assertEquals("Content-Type", response.headers()[0].name)
        assertEquals("application/json", response.headers()[0].value)
    }

    @Test
    fun deserializeObjectV2KnownExceptionReturnsItUnchanged() {
        val data = serialize(IllegalStateException("CE_1"), "[]")

        val response = AidlNetworkRequest.deserializeObjectV2(ByteArrayInputStream(data))

        assertTrue(response.exception() is IllegalStateException)
        assertEquals("CE_1", response.exception()?.message)
    }

    /** Mirrors `InputStreamBinder#serializeObjectToInputStreamV2` in the Files app. */
    private fun serialize(exception: Exception?, headers: String): ByteArray {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            oos.writeObject(exception)
            oos.writeObject(headers)
        }
        return baos.toByteArray()
    }

    private fun replaceBytes(data: ByteArray, search: String, replace: String): ByteArray {
        val searchBytes = search.toByteArray(StandardCharsets.US_ASCII)
        val replaceBytes = replace.toByteArray(StandardCharsets.US_ASCII)
        assertEquals(searchBytes.size, replaceBytes.size)

        val result = data.clone()
        var i = 0
        while (i <= result.size - searchBytes.size) {
            var match = true
            for (j in searchBytes.indices) {
                if (result[i + j] != searchBytes[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                System.arraycopy(replaceBytes, 0, result, i, replaceBytes.size)
            }
            i++
        }
        return result
    }
}
