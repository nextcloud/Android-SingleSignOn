/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021-2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2018-2019 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

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

    @WorkerThread
    private static String getCurrentAccountName(Context context) throws NoCurrentAccountSelectedException {
        SharedPreferences mPrefs = AccountImporter.getSharedPreferences(context);
        String accountName = mPrefs.getString(PREF_CURRENT_ACCOUNT_STRING, null);
        if (accountName == null) {
            throw new NoCurrentAccountSelectedException(context);
        }
        return accountName;
    }

    @WorkerThread
    public static SingleSignOnAccount getCurrentSingleSignOnAccount(Context context)
            throws NextcloudFilesAppAccountNotFoundException, NoCurrentAccountSelectedException {
        return AccountImporter.getSingleSignOnAccount(context, getCurrentAccountName(context));
    }

    /**
     * Emits the currently set {@link SingleSignOnAccount} or <code>null</code> if an error occurred.
     */
    public static LiveData<SingleSignOnAccount> getCurrentSingleSignOnAccount$(@NonNull Context context) {
        return new SingleSignOnAccountLiveData(context, AccountImporter.getSharedPreferences(context), PREF_CURRENT_ACCOUNT_STRING);
    }

    /**
     * Warning: This call is <em>synchronous</em>.
     * Consider using {@link #applyCurrentAccount(Context, String)} if possible.
     */
    @SuppressLint("ApplySharedPref")
    @WorkerThread
    public static void commitCurrentAccount(Context context, String accountName) {
        AccountImporter.getSharedPreferences(context)
                .edit()
                .putString(PREF_CURRENT_ACCOUNT_STRING, accountName)
                .commit();
    }

    public static void applyCurrentAccount(Context context, String accountName) {
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

}
