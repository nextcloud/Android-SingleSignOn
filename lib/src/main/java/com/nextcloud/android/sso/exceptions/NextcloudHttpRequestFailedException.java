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
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.R;

public class NextcloudHttpRequestFailedException extends SSOException {

    private final int statusCode;

    public NextcloudHttpRequestFailedException(@NonNull Context context, int statusCode, @Nullable Throwable cause) {
        super(
                context.getString(R.string.nextcloud_http_request_failed_message, statusCode),
                R.string.nextcloud_http_request_failed_title,
                cause
        );
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
