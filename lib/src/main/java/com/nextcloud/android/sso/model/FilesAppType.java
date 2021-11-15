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

package com.nextcloud.android.sso.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum FilesAppType {

    PROD("com.nextcloud.client", "nextcloud"),
    QA("com.nextcloud.android.qa", "nextcloud.qa"),
    DEV("com.nextcloud.android.beta", "nextcloud.beta");

    public final String packageId;
    public final String accountType;

    FilesAppType(@NonNull String packageId, @NonNull String accountType) {
        this.packageId = packageId;
        this.accountType = accountType;
    }

    /**
     * @return {@link #PROD}, {@link #QA} or {@link #DEV} depending on {@param accountType}.
     * Uses {@link #PROD} as fallback.
     */
    @NonNull
    public static FilesAppType findByAccountType(@Nullable String accountType) {
        for (final var appType : FilesAppType.values()) {
            if (appType.accountType.equalsIgnoreCase(accountType)) {
                return appType;
            }
        }
        return PROD;
    }
}
