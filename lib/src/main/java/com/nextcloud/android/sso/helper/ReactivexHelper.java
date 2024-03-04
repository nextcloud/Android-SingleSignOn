/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 sim <git@sgougeon.fr>
 * SPDX-FileCopyrightText: 2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.EmptyResponse;
import com.nextcloud.android.sso.api.NextcloudAPI;

import io.reactivex.Completable;

public final class ReactivexHelper {

    private ReactivexHelper() { }

    public static Completable wrapInCompletable(final NextcloudAPI nextcloudAPI, final NextcloudRequest request) {
        return Completable.fromAction(() -> nextcloudAPI.performRequestV2(EmptyResponse.class, request));
    }

    public static io.reactivex.rxjava3.core.Completable wrapInCompletableV3(final NextcloudAPI nextcloudAPI, final NextcloudRequest request) {
        return io.reactivex.rxjava3.core.Completable.fromAction(() -> nextcloudAPI.performRequestV2(EmptyResponse.class, request));
    }
}
