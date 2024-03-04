/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
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
