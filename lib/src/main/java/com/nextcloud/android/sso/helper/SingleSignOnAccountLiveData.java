/*
 * Nextcloud SingleSignOn
 *
 * @author Stefan Niedermann
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
