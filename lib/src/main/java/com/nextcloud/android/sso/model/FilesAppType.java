/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.model;

import androidx.annotation.NonNull;

public class FilesAppType {
    public final String packageId;
    public final String accountType;
    public final Type type;

    public FilesAppType(@NonNull String packageId, @NonNull String accountType, Type type) {
        this.packageId = packageId;
        this.accountType = accountType;
        this.type = type;
    }

    public enum Type {
        PROD, QA, DEV
    }
}
