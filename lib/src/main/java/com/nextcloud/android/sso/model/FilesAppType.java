/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.model;

import androidx.annotation.NonNull;

public record FilesAppType(@NonNull String packageId,
                           @NonNull String accountType,
                           @NonNull Stage stage) {

    public FilesAppType(@NonNull String accountType, @NonNull String packageId) {
        this(packageId, accountType, Stage.PROD);
    }

    public enum Stage {
        PROD, QA, DEV
    }
}
