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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.R;

public class NextcloudFilesAppAccountNotFoundException extends SSOException {

    public NextcloudFilesAppAccountNotFoundException(@NonNull Context context) {
        this(context, null);
    }

    public NextcloudFilesAppAccountNotFoundException(@NonNull Context context, @Nullable String accountName) {
        super(
                TextUtils.isEmpty(accountName)
                        ? context.getString(R.string.nextcloud_files_app_account_not_found_message)
                        : context.getString(R.string.nextcloud_files_app_account_not_found_with_account_message, accountName),
                R.string.nextcloud_files_app_account_not_found_title
        );
    }
}
