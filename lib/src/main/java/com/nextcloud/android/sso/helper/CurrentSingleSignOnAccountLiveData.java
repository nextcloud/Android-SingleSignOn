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

import static com.nextcloud.android.sso.helper.SingleAccountHelper.getCurrentSingleSignOnAccount;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

public class CurrentSingleSignOnAccountLiveData extends LiveData<SingleSignOnAccount> {

    private final Context context;
    private final SharedPreferences sharedPrefs;
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    CurrentSingleSignOnAccountLiveData(@NonNull Context context, @NonNull SharedPreferences sharedPrefs, @NonNull String key) {
        this.context = context;
        this.sharedPrefs = sharedPrefs;
        this.preferenceChangeListener = (changedPrefs, changedKey) -> {
            if (key.equals(changedKey)) {
                try {
                    setValue(getValueFromPreferences());
                } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        try {
            setValue(getValueFromPreferences());
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            e.printStackTrace();
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private SingleSignOnAccount getValueFromPreferences() throws NextcloudFilesAppAccountNotFoundException, NoCurrentAccountSelectedException {
        return getCurrentSingleSignOnAccount(context);
    }

    @Override
    protected void onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onInactive();
    }
}