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

public class NextcloudInvalidRequestUrlException extends SSOException {

    public NextcloudInvalidRequestUrlException(@NonNull Context context, @Nullable Throwable cause) {
        super(
                context.getString(R.string.nextcloud_invalid_request_url_message, cause == null ? context.getString(R.string.unknown_error_title) : cause.getMessage()),
                R.string.nextcloud_invalid_request_url_title
        );
    }
}
