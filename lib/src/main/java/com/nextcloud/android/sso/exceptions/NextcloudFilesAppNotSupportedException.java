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


public class NextcloudFilesAppNotSupportedException extends SSOException {

    public NextcloudFilesAppNotSupportedException(@NonNull Context context) {
        super(
                context.getString(R.string.nextcloud_files_app_not_supported_message),
                R.string.nextcloud_files_app_not_supported_title,
                R.string.nextcloud_files_app_not_supported_action,
                new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_files_app_marketplace)))
        );
    }
}
