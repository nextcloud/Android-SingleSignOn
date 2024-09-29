/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.model.FilesAppType;

import java.util.Collection;
import java.util.HashSet;

public class FilesAppTypeRegistry {
    private static final FilesAppTypeRegistry FILES_APP_TYPE_REGISTRY = new FilesAppTypeRegistry();
    private final Collection<FilesAppType> types = new HashSet<>();

    public FilesAppTypeRegistry() {
        types.add(new FilesAppType("com.nextcloud.client", "nextcloud", FilesAppType.Stage.PROD));
        types.add(new FilesAppType("com.nextcloud.android.qa", "nextcloud.qa", FilesAppType.Stage.QA));
        types.add(new FilesAppType("com.nextcloud.android.beta", "nextcloud.beta", FilesAppType.Stage.DEV));
    }

    public static FilesAppTypeRegistry getInstance() {
        return FILES_APP_TYPE_REGISTRY;
    }

    public synchronized void init(@NonNull FilesAppType type) {
        if (type.stage() != FilesAppType.Stage.PROD) {
            throw new IllegalArgumentException("If only one " + FilesAppType.class.getSimpleName() + " added, this must be " + FilesAppType.Stage.PROD.name() + "!");
        }

        types.clear();
        types.add(type);
    }

    public synchronized void init(@NonNull Collection<FilesAppType> types) {
        if (!types.stream().anyMatch(t -> t.stage() == FilesAppType.Stage.PROD)) {
            throw new IllegalArgumentException("At least one provided " + FilesAppType.class.getSimpleName() + " must be " + FilesAppType.Stage.PROD.name() + "!");
        }

        this.types.clear();
        this.types.addAll(types);
    }

    @NonNull
    public Collection<FilesAppType> getTypes() {
        return types;
    }

    @NonNull
    public String[] getAccountTypes() {
        return types
            .stream()
            .map(FilesAppType::accountType)
            .toArray(String[]::new);
    }


    /**
     * @return {@link FilesAppType.Stage#PROD}, {@link FilesAppType.Stage#QA}
     * or {@link FilesAppType.Stage#DEV} depending on {@param accountType}.
     * Uses {@link FilesAppType.Stage#PROD} as fallback.
     */
    @NonNull
    public FilesAppType findByAccountType(@Nullable String accountType) {
        for (final var type : types) {
            if (type.accountType().equalsIgnoreCase(accountType)) {
                return type;
            }
        }

        return types
            .stream()
            .filter(t -> t.stage() == FilesAppType.Stage.PROD)
            .findFirst()
            .get();
    }
}
