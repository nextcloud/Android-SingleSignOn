/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2019-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.exceptions;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.R;

public class AccountImportCancelledException extends SSOException {

    public AccountImportCancelledException(@NonNull Context context) {
        super(
                context.getString(R.string.sso_canceled_message),
                R.string.sso_canceled
        );
    }
}
