/*
 * Nextcloud SingleSignOn
 *
 * @author David Luhmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextcloud.android.sso.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.model.FilesAppType;
import com.nextcloud.android.sso.ui.UiExceptionManager;

public final class VersionCheckHelper {

    private static final String TAG = VersionCheckHelper.class.getCanonicalName();

    private VersionCheckHelper() { }

    /**
     * @deprecated Use {@link #verifyMinVersion(Context, int, FilesAppType)}
     */
    @Deprecated
    public static boolean verifyMinVersion(Activity activity, int minVersion) {
        return verifyMinVersion(activity, minVersion, FilesAppType.PROD);
    }

    public static boolean verifyMinVersion(@NonNull Context context, int minVersion, @NonNull FilesAppType type) {
        try {
            final int verCode = getNextcloudFilesVersionCode(context, type);
            if (verCode < minVersion) {
                UiExceptionManager.showDialogForException(context, new NextcloudFilesAppNotSupportedException());
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager.NameNotFoundException (prod files app not found): " + e.getMessage());

            // Stable Files App is not installed at all. Therefore we need to run the test on the dev app
            try {
                final int verCode = getNextcloudFilesVersionCode(context, type);
                // The dev app follows a different versioning schema.. therefore we can't do our normal checks
                // However beta users are probably always up to date so we will just ignore it for now
                Log.d(TAG, "Dev files app version is: " + verCode);
                /*
                if (verCode < minVersion) {
                    UiExceptionManager.showDialogForException(activity, new NextcloudFilesAppNotSupportedException());
                    return false;
                }
                */
                return true;
            } catch (PackageManager.NameNotFoundException ex) {
                Log.e(TAG, "PackageManager.NameNotFoundException (dev files app not found): " + e.getMessage());
                UiExceptionManager.showDialogForException(context, new NextcloudFilesAppNotInstalledException());
            }
        }
        return false;
    }

    /**
     * @deprecated use {@link #getNextcloudFilesVersionCode(Context, FilesAppType)}
     */
    @Deprecated
    public static int getNextcloudFilesVersionCode(@NonNull Context context) throws PackageManager.NameNotFoundException {
        return getNextcloudFilesVersionCode(context, FilesAppType.PROD);
    }

    /**
     * @deprecated use {@link #getNextcloudFilesVersionCode(Context, FilesAppType)}
     */
    @Deprecated
    public static int getNextcloudFilesVersionCode(@NonNull Context context, boolean prod) throws PackageManager.NameNotFoundException {
        return getNextcloudFilesVersionCode(context, prod ? FilesAppType.PROD : FilesAppType.DEV);
    }

    public static int getNextcloudFilesVersionCode(@NonNull Context context, @NonNull FilesAppType appType) throws PackageManager.NameNotFoundException {
        final PackageInfo pInfo = context.getPackageManager().getPackageInfo(appType.packageId, 0);
        final int verCode = pInfo.versionCode;
        Log.d("VersionCheckHelper", "Version Code: " + verCode);
        return verCode;
    }
}
