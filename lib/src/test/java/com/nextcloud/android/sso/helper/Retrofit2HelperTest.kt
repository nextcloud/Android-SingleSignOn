/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2017-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2025 Alper Ozturk <alper.ozturk@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper

import com.nextcloud.android.sso.api.AidlNetworkRequest
import com.nextcloud.android.sso.helper.Retrofit2Helper.buildHeaders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("MagicNumber", "TooManyFunctions")
class Retrofit2HelperTest {
    @Test
    fun testBuildHeadersWhenGivenNullHeadersShouldReturnEmptyHeaders() {
        val headers = buildHeaders(null)
        assertEquals(0, headers.size)
    }

    @Test
    fun testBuildHeadersWhenGivenEmptyHeadersListShouldReturnEmptyHeaders() {
        val headers = buildHeaders(emptyList())
        assertEquals(0, headers.size)
    }

    @Test
    fun testBuildHeadersWhenGivenSingleHeaderShouldReturnThatHeader() {
        val plainHeaders =
            listOf(
                createPlainHeader("Content-Type", "application/json")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(1, headers.size)
        assertEquals("application/json", headers["Content-Type"])
    }

    @Test
    fun testBuildHeadersWhenGivenDuplicateHeadersShouldRemoveDuplicates() {
        val plainHeaders =
            listOf(
                createPlainHeader("X-Robots-Tag", "noindex, nofollow"),
                createPlainHeader("X-Robots-Tag", "noindex, nofollow"),
                createPlainHeader("X-Robots-Tag", "noindex, nofollow")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(1, headers.size)
        assertEquals("noindex, nofollow", headers["X-Robots-Tag"])
    }

    @Test
    fun testBuildHeadersWhenGivenMultipleDistinctValuesShouldKeepAllValues() {
        val plainHeaders =
            listOf(
                createPlainHeader("Set-Cookie", "session=abc123"),
                createPlainHeader("Set-Cookie", "token=xyz789"),
                createPlainHeader("Set-Cookie", "user=john")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(3, headers.size)
        val cookies = headers.values("Set-Cookie")
        assertEquals(3, cookies.size)
        assertTrue(cookies.contains("session=abc123"))
        assertTrue(cookies.contains("token=xyz789"))
        assertTrue(cookies.contains("user=john"))
    }

    @Test
    fun testBuildHeadersWhenGivenMixedDuplicateAndDistinctValuesShouldHandleCorrectly() {
        val plainHeaders =
            listOf(
                createPlainHeader("Set-Cookie", "session=abc"),
                createPlainHeader("Set-Cookie", "session=abc"),
                createPlainHeader("Set-Cookie", "token=xyz"),
                createPlainHeader("X-Robots-Tag", "noindex"),
                createPlainHeader("X-Robots-Tag", "noindex")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(3, headers.size)

        val cookies = headers.values("Set-Cookie")
        assertEquals(2, cookies.size)
        assertTrue(cookies.contains("session=abc"))
        assertTrue(cookies.contains("token=xyz"))

        assertEquals("noindex", headers["X-Robots-Tag"])
    }

    @Test
    fun testBuildHeadersWhenGivenCaseInsensitiveHeaderNamesShouldTreatAsSame() {
        val plainHeaders =
            listOf(
                createPlainHeader("Content-Type", "application/json"),
                createPlainHeader("content-type", "application/json"),
                createPlainHeader("CONTENT-TYPE", "application/json")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(1, headers.size)
        assertEquals("application/json", headers["Content-Type"])
    }

    @Test
    fun testBuildHeadersWhenGivenCaseInsensitiveHeaderNamesWithDifferentValuesShouldKeepAll() {
        val plainHeaders =
            listOf(
                createPlainHeader("Content-Type", "application/json"),
                createPlainHeader("content-type", "text/html"),
                createPlainHeader("CONTENT-TYPE", "application/xml")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(3, headers.size)
        val values = headers.values("Content-Type")
        assertTrue(values.contains("application/json"))
        assertTrue(values.contains("text/html"))
        assertTrue(values.contains("application/xml"))
    }

    @Test
    fun testBuildHeadersWhenGivenSameNameDifferentValuesShouldKeepAll() {
        val plainHeaders =
            listOf(
                createPlainHeader("Accept", "text/html"),
                createPlainHeader("Accept", "application/json"),
                createPlainHeader("Accept", "text/plain")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(3, headers.size)
        val accepts = headers.values("Accept")
        assertEquals(3, accepts.size)
        assertTrue(accepts.contains("text/html"))
        assertTrue(accepts.contains("application/json"))
        assertTrue(accepts.contains("text/plain"))
    }

    @Test
    fun testBuildHeadersWhenGivenComplexScenarioShouldHandleAllCasesCorrectly() {
        val plainHeaders =
            listOf(
                createPlainHeader("Content-Type", "application/json"),
                createPlainHeader("Set-Cookie", "session=abc"),
                createPlainHeader("Set-Cookie", "token=xyz"),
                createPlainHeader("Set-Cookie", "session=abc"),
                createPlainHeader("X-Robots-Tag", "noindex, nofollow"),
                createPlainHeader("X-Robots-Tag", "noindex, nofollow"),
                createPlainHeader("Cache-Control", "no-cache"),
                createPlainHeader("cache-control", "no-cache"),
                createPlainHeader("Accept", "text/html"),
                createPlainHeader("Accept", "application/json")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(7, headers.size)

        assertEquals("application/json", headers["Content-Type"])

        val cookies = headers.values("Set-Cookie")
        assertEquals(2, cookies.size)
        assertTrue(cookies.contains("session=abc"))
        assertTrue(cookies.contains("token=xyz"))

        assertEquals("noindex, nofollow", headers["X-Robots-Tag"])
        assertEquals("no-cache", headers["Cache-Control"])

        val accepts = headers.values("Accept")
        assertEquals(2, accepts.size)
        assertTrue(accepts.contains("text/html"))
        assertTrue(accepts.contains("application/json"))
    }

    @Test
    fun testBuildHeadersWhenGivenHeadersWithWhitespaceShouldPreserveExactValue() {
        val plainHeaders =
            listOf(
                createPlainHeader("X-Custom", "value with spaces"),
                createPlainHeader("X-Custom", "value with spaces")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(1, headers.size)
        assertEquals("value with spaces", headers["X-Custom"])
    }

    @Test
    fun testBuildHeadersWhenGivenHeadersWithSpecialCharactersShouldPreserveExactValue() {
        val plainHeaders =
            listOf(
                createPlainHeader("Authorization", "Bearer eyJhbGc..."),
                createPlainHeader("X-Special", "value=test;path=/;secure")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(2, headers.size)
        assertEquals("Bearer eyJhbGc...", headers["Authorization"])
        assertEquals("value=test;path=/;secure", headers["X-Special"])
    }

    @Test
    fun testBuildHeadersWhenGivenEmptyHeaderValueShouldHandleCorrectly() {
        val plainHeaders =
            listOf(
                createPlainHeader("X-Empty", ""),
                createPlainHeader("X-Empty", "")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(1, headers.size)
        assertEquals("", headers["X-Empty"])
    }

    @Test
    fun testBuildHeadersWhenGivenMultipleHeadersWithSomeEmptyValuesShouldHandleCorrectly() {
        val plainHeaders =
            listOf(
                createPlainHeader("X-Test", "value1"),
                createPlainHeader("X-Test", ""),
                createPlainHeader("X-Test", "value2"),
                createPlainHeader("X-Test", "")
            )

        val headers = buildHeaders(plainHeaders)

        assertEquals(3, headers.size)
        val values = headers.values("X-Test")
        assertEquals(3, values.size)
        assertTrue(values.contains("value1"))
        assertTrue(values.contains(""))
        assertTrue(values.contains("value2"))
    }

    private fun createPlainHeader(
        name: String,
        value: String
    ) = AidlNetworkRequest.PlainHeader(name, value)
}
