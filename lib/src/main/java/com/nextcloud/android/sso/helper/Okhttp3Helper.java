/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2018 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import android.util.Log;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;

import java.io.InputStream;

import okhttp3.ResponseBody;

public final class Okhttp3Helper {

    private final static String TAG = Okhttp3Helper.class.getCanonicalName();

    private Okhttp3Helper() { }

    public static ResponseBody getResponseBodyFromRequestV2(NextcloudAPI nextcloudAPI, NextcloudRequest request) {
        try {
            final InputStream os = nextcloudAPI.performNetworkRequestV2(request).getBody();
            return ResponseBody.create(null, 0, new BufferedSourceSSO(os));
        } catch (Exception e) {
            Log.e(TAG, "[getResponseBodyFromRequestV2] encountered a problem", e);
        }
        return ResponseBody.create(null, "");
    }
}
