/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.exceptions;

import com.nextcloud.android.sso.R;

public class UnknownErrorException extends SSOException {

    public UnknownErrorException(String message) {
        super(message, R.string.unknown_error_title);
    }
}
