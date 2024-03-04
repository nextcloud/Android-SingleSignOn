/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021-2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleSignOnAccountLiveData extends LiveData<SingleSignOnAccount> {

    private final Context context;
    private final SharedPreferences sharedPrefs;
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private final ExecutorService executor;

    SingleSignOnAccountLiveData(@NonNull Context context,
                                @NonNull SharedPreferences sharedPrefs,
                                @NonNull String key) {
        this(context, sharedPrefs, key, Executors.newSingleThreadExecutor());
    }

    @VisibleForTesting
    SingleSignOnAccountLiveData(@NonNull Context context,
                                @NonNull SharedPreferences sharedPrefs,
                                @NonNull String key,
                                @NonNull ExecutorService executor) {
        this.context = context;
        this.sharedPrefs = sharedPrefs;
        this.executor = executor;
        this.preferenceChangeListener = (changedPrefs, changedKey) -> {
            if (key.equals(changedKey)) {
                postValueFromPreferences();
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        postValueFromPreferences();
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onInactive();
    }

    private void postValueFromPreferences() {
        executor.submit(() -> {
            try {
                postValue(SingleAccountHelper.getCurrentSingleSignOnAccount(context));
            } catch (NoCurrentAccountSelectedException | NextcloudFilesAppAccountNotFoundException e) {
                postValue(null);
                e.printStackTrace();
            }
        });
    }
}
