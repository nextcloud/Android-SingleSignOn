package com.nextcloud.android.sso.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
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

public final class FilesAppNotInstalledHelperUtil {

    private FilesAppNotInstalledHelperUtil() { }

    public static void requestInstallNextcloudFilesApp(Context context) {
        // Nextcloud app not installed
        Intent installIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nextcloud.client"));

        // launch market(s)
        if (installIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(installIntent);
        } else {
            // no f-droid market app or Play store installed --> launch browser for f-droid url
            Intent downloadIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/repository/browse/?fdid=com.nextcloud.client"));
            context.startActivity(downloadIntent);
        }
    }
}
