/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2020 Desperate Coder <echotodevnull@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.api;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParsedResponse<T> {
    private final T response;
    private final Map<String, String> headers = new HashMap<>();

    public ParsedResponse(T response, @Nullable ArrayList<AidlNetworkRequest.PlainHeader> headers) {
        this.response = response;
        if (headers != null) {
            for (AidlNetworkRequest.PlainHeader header : headers) {
                this.headers.put(header.getName(), header.getValue());
            }
        }
    }

    public static <T> ParsedResponse<T> of(T data) {
        return new ParsedResponse<>(data, null);
    }

    public static <T> ParsedResponse<T> of(T data, ArrayList<AidlNetworkRequest.PlainHeader> headers) {
        return new ParsedResponse<T>(data, headers);
    }

    public T getResponse() {
        return response;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
