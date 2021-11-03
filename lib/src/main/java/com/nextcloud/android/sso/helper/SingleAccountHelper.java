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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountPermissionNotGrantedException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

public final class SingleAccountHelper {

    private static final String PREF_CURRENT_ACCOUNT_STRING = "PREF_CURRENT_ACCOUNT_STRING";

    private SingleAccountHelper() {
    }

    private static String getCurrentAccountName(Context context) throws NoCurrentAccountSelectedException {
        SharedPreferences mPrefs = AccountImporter.getSharedPreferences(context);
        String accountName = mPrefs.getString(PREF_CURRENT_ACCOUNT_STRING, null);
        if (accountName == null) {
            throw new NoCurrentAccountSelectedException();
        }
        return accountName;
    }

    public static SingleSignOnAccount getCurrentSingleSignOnAccount(Context context)
            throws NextcloudFilesAppAccountNotFoundException, NoCurrentAccountSelectedException {
        return AccountImporter.getSingleSignOnAccount(context, getCurrentAccountName(context));
    }

    /**
     * Warning: This call is writing synchronously to the disk.
     * You should use {@link #setCurrentAccountAsync(Context, String)} if possible.
     */
    @SuppressLint("ApplySharedPref")
    public static void setCurrentAccount(Context context, String accountName) {
        AccountImporter.getSharedPreferences(context)
                .edit()
                .putString(PREF_CURRENT_ACCOUNT_STRING, accountName)
                .commit();
    }

    public static void setCurrentAccountAsync(Context context, String accountName) {
        AccountImporter.getSharedPreferences(context)
                .edit()
                .putString(PREF_CURRENT_ACCOUNT_STRING, accountName)
                .apply();
    }

    public static void reauthenticateCurrentAccount(Fragment fragment) throws NoCurrentAccountSelectedException, NextcloudFilesAppAccountNotFoundException, NextcloudFilesAppNotSupportedException, NextcloudFilesAppAccountPermissionNotGrantedException {
        AccountImporter.authenticateSingleSignAccount(fragment, getCurrentSingleSignOnAccount(fragment.getContext()));
    }

    public static void reauthenticateCurrentAccount(Activity activity) throws NoCurrentAccountSelectedException, NextcloudFilesAppAccountNotFoundException, NextcloudFilesAppNotSupportedException, NextcloudFilesAppAccountPermissionNotGrantedException {
        AccountImporter.authenticateSingleSignAccount(activity, getCurrentSingleSignOnAccount(activity));
    }
    
    public static void registerSharedPreferenceChangeListener(Context context, 
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        AccountImporter.getSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(listener);
    }
    
    public static void unregisterSharedPreferenceChangeListener(Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        AccountImporter.getSharedPreferences(context)
                .unregisterOnSharedPreferenceChangeListener(listener);
        
    }
}
