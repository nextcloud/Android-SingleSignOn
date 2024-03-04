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
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.R;

public class NoCurrentAccountSelectedException extends SSOException {

    public NoCurrentAccountSelectedException(@NonNull Context context) {
        super(
                context.getString(R.string.no_current_account_selected_exception_message),
                R.string.no_current_account_selected_exception_title,
                R.string.no_current_account_selected_exception_action,
                new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_sso_documentation)))
        );
    }
}
