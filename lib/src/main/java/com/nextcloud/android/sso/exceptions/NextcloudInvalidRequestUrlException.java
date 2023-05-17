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

public class NextcloudInvalidRequestUrlException extends SSOException {

    public NextcloudInvalidRequestUrlException(@NonNull Context context, @Nullable Throwable cause) {
        super(
                context.getString(R.string.nextcloud_invalid_request_url_message, cause == null ? context.getString(R.string.unknown_error_title) : cause.getMessage()),
                R.string.nextcloud_invalid_request_url_title
        );
    }
}
