/*
 *  Nextcloud SingleSignOn
 *
 *  @author David Luhmer
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

package com.nextcloud.android.sso.exceptions;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.R;

public class NextcloudApiNotRespondingException extends SSOException {

    public NextcloudApiNotRespondingException(@NonNull Context context) {
        super(
                context.getString(R.string.nextcloud_files_api_not_responding_message),
                R.string.nextcloud_files_api_not_responding_title,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? R.string.nextcloud_files_api_not_responding_action
                        : null,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? new Intent().setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        : null
        );
    }

}
