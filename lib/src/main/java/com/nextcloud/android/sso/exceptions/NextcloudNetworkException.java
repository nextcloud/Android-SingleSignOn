/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2025 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.exceptions;

import androidx.annotation.Nullable;
import com.nextcloud.android.sso.R;

public class NextcloudNetworkException extends SSOException {
    public NextcloudNetworkException(@Nullable Throwable cause) {
        super("Network connection failed. Please check your internet connection and server URL.",
            R.string.network_error_title,
            R.string.retry_action,
            null,
            cause);
    }
}
