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

public class NextcloudFilesAppNotInstalledException extends SSOException {

    public NextcloudFilesAppNotInstalledException(@NonNull Context context) {
        this(context, new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_files_app_marketplace))));
    }

    private NextcloudFilesAppNotInstalledException(@NonNull Context context,
                                                   @NonNull Intent launchStoreIntent) {
        this(context, launchStoreIntent, launchStoreIntent.resolveActivity(context.getPackageManager()) != null);
    }

    private NextcloudFilesAppNotInstalledException(@NonNull Context context,
                                                   @NonNull Intent launchStoreIntent,
                                                   boolean storeAvailable) {
        super(
                storeAvailable ? context.getString(R.string.nextcloud_files_app_not_installed_message) : context.getString(R.string.nextcloud_files_app_no_store_installed_message),
                storeAvailable ? R.string.nextcloud_files_app_not_installed_title : R.string.nextcloud_files_app_no_store_installed_title,
                storeAvailable ? R.string.nextcloud_files_app_not_installed_action : R.string.nextcloud_files_app_no_store_installed_action,
                storeAvailable ? launchStoreIntent : new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_install_nextcloud_client)))
        );
    }
}
