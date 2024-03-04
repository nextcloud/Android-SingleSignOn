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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.R;

public class NextcloudFilesAppAccountNotFoundException extends SSOException {

    public NextcloudFilesAppAccountNotFoundException(@NonNull Context context) {
        this(context, null);
    }

    public NextcloudFilesAppAccountNotFoundException(@NonNull Context context, @Nullable String accountName) {
        super(
                TextUtils.isEmpty(accountName)
                        ? context.getString(R.string.nextcloud_files_app_account_not_found_message)
                        : context.getString(R.string.nextcloud_files_app_account_not_found_with_account_message, accountName),
                R.string.nextcloud_files_app_account_not_found_title
        );
    }
}
