/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021-2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018-2022 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.FilesAppTypeRegistry;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.model.FilesAppType;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import java.util.Optional;

public final class VersionCheckHelper {

    private static final String TAG = VersionCheckHelper.class.getCanonicalName();

    private VersionCheckHelper() { }

    public static boolean verifyMinVersion(@NonNull Context context, int minVersion, @NonNull FilesAppType type) {
        try {
            final int versionCode = getNextcloudFilesVersionCode(context, type);
            if (versionCode < minVersion) {
                UiExceptionManager.showDialogForException(context, new NextcloudFilesAppNotSupportedException(context));
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager.NameNotFoundException (" + type + " files app not found): " + e.getMessage());

            // Stable Files App is not installed at all. Therefore we need to run the test on the dev app
            try {
                Optional<FilesAppType> dev = FilesAppTypeRegistry
                    .getInstance()
                    .getTypes()
                    .stream()
                    .filter(t -> t.stage() == FilesAppType.Stage.DEV)
                    .findFirst();
                if (dev.isPresent()) {
                    final int verCode = getNextcloudFilesVersionCode(context, dev.get());
                    // The dev app follows a different versioning schema.. therefore we can't do our normal checks

                    // However beta users are probably always up to date so we will just ignore it for now
                    Log.d(TAG, "Dev files app version is: " + verCode);
                    return true;
                } else {
                    UiExceptionManager.showDialogForException(context, new NextcloudFilesAppNotInstalledException(context));
                }
            } catch (PackageManager.NameNotFoundException ex) {
                Log.e(TAG, "PackageManager.NameNotFoundException (dev files app not found): " + e.getMessage());
                UiExceptionManager.showDialogForException(context, new NextcloudFilesAppNotInstalledException(context));
            }
        }
        return false;
    }

    public static int getNextcloudFilesVersionCode(@NonNull Context context, @NonNull FilesAppType appType) throws PackageManager.NameNotFoundException {
        final var packageInfo = context.getPackageManager().getPackageInfo(appType.packageId(), 0);
        final int verCode = packageInfo.versionCode;
        Log.d("VersionCheckHelper", "Version Code: " + verCode);
        return verCode;
    }
}
