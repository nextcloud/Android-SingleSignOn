package com.nextcloud.android.sso.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.ui.UiExceptionManager;

public class VersionCheckHelper {

    private static final String TAG = VersionCheckHelper.class.getCanonicalName();

    public static boolean VerifyMinVersion(Activity activity, int minVersion) {
        try {
            int verCode = GetNextcloudFilesVersionCode(activity);

            if (verCode < minVersion) {
                UiExceptionManager.ShowDialogForException(activity, new NextcloudFilesAppNotSupportedException());
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager.NameNotFoundException: " + e.getLocalizedMessage());
            //e.printStackTrace();
            UiExceptionManager.ShowDialogForException(activity, new NextcloudFilesAppNotInstalledException());
            return false;
        }

        return true;
    }

    public static int GetNextcloudFilesVersionCode(Activity activity) throws PackageManager.NameNotFoundException {
        PackageInfo pinfo = activity.getPackageManager().getPackageInfo("com.nextcloud.client", 0);
        int verCode = pinfo.versionCode;
        Log.e("VersionCheckHelper", "Version Code: " + verCode);
        return verCode;
    }
}
