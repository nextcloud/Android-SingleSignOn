/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.exceptions;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.R;

public class NextcloudHttpRequestFailedException extends SSOException {

    private final int statusCode;

    public NextcloudHttpRequestFailedException(@NonNull Context context, int statusCode, @Nullable Throwable cause) {
        super(
                context.getString(R.string.nextcloud_http_request_failed_message, statusCode),
                R.string.nextcloud_http_request_failed_title,
                cause
        );
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
