/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso;

import com.nextcloud.android.sso.model.FilesAppType;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FilesAppTypeRegistry {
    private static final FilesAppTypeRegistry FILES_APP_TYPE_REGISTRY = new FilesAppTypeRegistry();
    private final Set<FilesAppType> types = new HashSet<>();

    public FilesAppTypeRegistry() {
        types.add(new FilesAppType("com.nextcloud.client", "nextcloud", FilesAppType.Type.PROD));
        types.add(new FilesAppType("com.nextcloud.android.qa", "nextcloud.qa", FilesAppType.Type.QA));
        types.add(new FilesAppType("com.nextcloud.android.beta", "nextcloud.beta", FilesAppType.Type.DEV));
    }

    public static FilesAppTypeRegistry getInstance() {
        return FILES_APP_TYPE_REGISTRY;
    }

    public synchronized void init(FilesAppType type) {
        types.clear();

        if (type.type != FilesAppType.Type.PROD) {
            throw new IllegalArgumentException("If only one FilesAppType added, this must be PROD!");
        }
        
        types.add(type);
    }

    public synchronized void init(List<FilesAppType> types) {
        this.types.clear();

        Optional<FilesAppType> prod = types.stream().filter(t -> t.type == FilesAppType.Type.PROD).findFirst();
        if (prod.isEmpty()) {
            throw new IllegalArgumentException("One provided FilesAppType must be PROD!");
        }

        this.types.addAll(types);
    }

    public Set<FilesAppType> getTypes() {
        return types;
    }

    public String[] getAccountTypes() {
        return types.stream().map(a -> a.accountType).toArray(String[]::new);
    }


    /**
     * @return {@link FilesAppType.Type#PROD}, {@link FilesAppType.Type#QA}
     * or {@link FilesAppType.Type#DEV} depending on {@param accountType}.
     * Uses {@link FilesAppType.Type#PROD} as fallback.
     */
    @NonNull
    public FilesAppType findByAccountType(@Nullable String accountType) {
        for (final var type : types) {
            if (type.accountType.equalsIgnoreCase(accountType)) {
                return type;
            }
        }
        return types.stream().filter(t -> t.type == FilesAppType.Type.PROD).findFirst().get();
    }
}
