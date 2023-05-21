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

public class NoCurrentAccountSelectedException extends SSOException {

    public NoCurrentAccountSelectedException(@NonNull Context context) {
        super(
                context.getString(R.string.no_current_account_selected_exception_message),
                R.string.no_current_account_selected_exception_title,
                R.string.no_current_account_selected_exception_action,
                new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_sso_documentation)))
        );
    }
}
