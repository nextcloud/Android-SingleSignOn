/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.api;

import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.ArrayList;

public class Response {
    private final InputStream body;
    private final ArrayList<AidlNetworkRequest.PlainHeader> headers;

    public Response(InputStream inputStream, ArrayList<AidlNetworkRequest.PlainHeader> headers) {
        this.body = inputStream;
        this.headers = headers;
    }

    public ArrayList<AidlNetworkRequest.PlainHeader> getPlainHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }
    
    @Nullable
    public AidlNetworkRequest.PlainHeader getPlainHeader(String key) {
        for (AidlNetworkRequest.PlainHeader header: headers) {
            if (header.getName().equalsIgnoreCase(key)) {
                return header;
            }
        }
        
        return null;
    }
}
