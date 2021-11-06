package com.nextcloud.android.sso.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.Constants;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.ui.UiExceptionManager;

public final class VersionCheckHelper {

    private static final String TAG = VersionCheckHelper.class.getCanonicalName();

    private VersionCheckHelper() { }

    public static boolean verifyMinVersion(@NonNull Context context, int minVersion) {
        try {
            final int verCode = getNextcloudFilesVersionCode(context, true);
            if (verCode < minVersion) {
                UiExceptionManager.showDialogForException(context, new NextcloudFilesAppNotSupportedException());
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager.NameNotFoundException (prod files app not found): " + e.getMessage());

            // Stable Files App is not installed at all. Therefore we need to run the test on the dev app
            try {
                final int verCode = getNextcloudFilesVersionCode(context, false);
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
     * @param context {@link Context}
     * @param prod    if <code>true</code>, the version of {@link Constants#PACKAGE_NAME_PROD} is checked, otherwise {@link Constants#PACKAGE_NAME_DEV}.
     */
    public static int getNextcloudFilesVersionCode(@NonNull Context context, boolean prod) throws PackageManager.NameNotFoundException {
        final PackageInfo pInfo = context.getPackageManager().getPackageInfo(prod ? Constants.PACKAGE_NAME_PROD : Constants.PACKAGE_NAME_DEV, 0);
        final int verCode = pInfo.versionCode;
        Log.d(TAG, "Version Code: " + verCode);
        return verCode;
    }
}
