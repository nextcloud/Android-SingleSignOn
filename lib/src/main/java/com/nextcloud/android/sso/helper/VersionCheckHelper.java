package com.nextcloud.android.sso.helper;

import android.app.Activity;
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

    public static boolean verifyMinVersion(Activity activity, int minVersion) {
        try {
            final int verCode = getNextcloudFilesVersionCode(activity, true);
            if (verCode < minVersion) {
                UiExceptionManager.showDialogForException(activity, new NextcloudFilesAppNotSupportedException());
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager.NameNotFoundException (prod files app not found): " + e.getMessage());

            // Stable Files App is not installed at all. Therefore we need to run the test on the dev app
            try {
                final int verCode = getNextcloudFilesVersionCode(activity, false);
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
                UiExceptionManager.showDialogForException(activity, new NextcloudFilesAppNotInstalledException());
            }
        }
        return false;
    }

    /**
     * @deprecated use {@link #getNextcloudFilesVersionCode(Context, boolean)}
     */
    @Deprecated
    public static int getNextcloudFilesVersionCode(@NonNull Context context) throws PackageManager.NameNotFoundException {
        return getNextcloudFilesVersionCode(context, true);
    }

    public static int getNextcloudFilesVersionCode(@NonNull Context context, boolean prod) throws PackageManager.NameNotFoundException {
        final PackageInfo pInfo = context.getPackageManager().getPackageInfo(prod ? Constants.PACKAGE_NAME_PROD : Constants.PACKAGE_NAME_DEV, 0);
        final int verCode = pInfo.versionCode;
        Log.d("VersionCheckHelper", "Version Code: " + verCode);
        return verCode;
    }
}
