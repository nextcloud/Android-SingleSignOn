/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.R;

public final class FilesAppNotInstalledHelperUtil {

    private FilesAppNotInstalledHelperUtil() { }

    public static void requestInstallNextcloudFilesApp(@NonNull Context context) {
        // Nextcloud app not installed
        final var installIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_files_app_marketplace)));

        // launch market(s)
        if (installIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(installIntent);
        } else {
            // no F-Droid market app or Play store installed → launch browser for f-droid url
            final var downloadIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_files_app_fdroid)));
            context.startActivity(downloadIntent);
        }
    }
}
