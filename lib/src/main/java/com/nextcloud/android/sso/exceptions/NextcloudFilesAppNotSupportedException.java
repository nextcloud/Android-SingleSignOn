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

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.R;
import com.nextcloud.android.sso.model.ExceptionMessage;


public class NextcloudFilesAppNotSupportedException extends SSOException {

    @Override
    public void loadExceptionMessage(@NonNull Context context) {
        this.em = new ExceptionMessage(
                context.getString(R.string.nextcloud_files_app_not_supported_title),
                context.getString(
                        R.string.nextcloud_files_app_not_supported_message,
                        "https://play.google.com/store/apps/details?id=com.nextcloud.client")
        );
    }
}
