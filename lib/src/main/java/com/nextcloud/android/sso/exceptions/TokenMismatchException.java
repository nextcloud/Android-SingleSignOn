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

import com.nextcloud.android.sso.R;

public class TokenMismatchException extends SSOException {

    public TokenMismatchException(@NonNull Context context) {
        super(context.getString(R.string.token_mismatch_message), R.string.token_mismatch_title);
    }
}
