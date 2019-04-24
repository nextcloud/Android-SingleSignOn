package com.nextcloud.android.sso.helper;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.ui.UiExceptionManager;

public final class VersionCheckHelper {

    private static final String TAG = VersionCheckHelper.class.getCanonicalName();

    private VersionCheckHelper() { }

    public static boolean verifyMinVersion(Activity activity, int minVersion) {
        try {
            int verCode = getNextcloudFilesVersionCode(activity);

            if (verCode < minVersion) {
                UiExceptionManager.showDialogForException(activity, new NextcloudFilesAppNotSupportedException());
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager.NameNotFoundException (app with package-name not found): " + e.getMessage());
            UiExceptionManager.showDialogForException(activity, new NextcloudFilesAppNotInstalledException());
            return false;
        }

        return true;
    }

    public static int getNextcloudFilesVersionCode(Activity activity) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = activity.getPackageManager().getPackageInfo("com.nextcloud.client", 0);
        int verCode = pInfo.versionCode;
        Log.e("VersionCheckHelper", "Version Code: " + verCode);
        return verCode;
    }
}
