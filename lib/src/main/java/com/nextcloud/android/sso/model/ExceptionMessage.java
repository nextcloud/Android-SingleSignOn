package com.nextcloud.android.sso.model;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.Serializable;

/**
 * Nextcloud SingleSignOn
 *
 * @author David Luhmer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ExceptionMessage implements Serializable {

    @NonNull
    public final String title;
    @NonNull
    public final String message;
    @Nullable
    @StringRes
    public final Integer actionText;
    @Nullable
    public final Intent actionIntent;

    public ExceptionMessage(@NonNull String title,
                            @NonNull String message) {
        this(title, message, null, null);
    }

    public ExceptionMessage(@NonNull String title,
                            @NonNull String message,
                            @Nullable @StringRes Integer actionText,
                            @Nullable Intent actionIntent) {
        this.title = title;
        this.message = message;
        this.actionText = actionText;
        this.actionIntent = actionIntent;
    }

}
