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
import android.net.Uri;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.R;

public class NextcloudFilesAppNotInstalledException extends SSOException {

    public NextcloudFilesAppNotInstalledException(@NonNull Context context) {
        this(context, new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_files_app_marketplace))));
    }

    private NextcloudFilesAppNotInstalledException(@NonNull Context context,
                                                   @NonNull Intent launchStoreIntent) {
        this(context, launchStoreIntent, launchStoreIntent.resolveActivity(context.getPackageManager()) != null);
    }

    private NextcloudFilesAppNotInstalledException(@NonNull Context context,
                                                   @NonNull Intent launchStoreIntent,
                                                   boolean storeAvailable) {
        super(
                storeAvailable ? context.getString(R.string.nextcloud_files_app_not_installed_message) : context.getString(R.string.nextcloud_files_app_no_store_installed_message),
                storeAvailable ? R.string.nextcloud_files_app_not_installed_title : R.string.nextcloud_files_app_no_store_installed_title,
                storeAvailable ? R.string.nextcloud_files_app_not_installed_action : R.string.nextcloud_files_app_no_store_installed_action,
                storeAvailable ? launchStoreIntent : new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_install_nextcloud_client)))
        );
    }
}
