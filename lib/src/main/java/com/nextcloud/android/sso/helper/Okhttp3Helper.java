package com.nextcloud.android.sso.helper;

import android.util.Log;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;

import java.io.InputStream;

import okhttp3.ResponseBody;

/**
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

public final class Okhttp3Helper {

    private final static String TAG = Okhttp3Helper.class.getCanonicalName();

    private Okhttp3Helper() { }

    /**
     * @deprecated Use {@link #getResponseBodyFromRequestV2(NextcloudAPI, NextcloudRequest)}
     * @see <a href="https://github.com/nextcloud/Android-SingleSignOn/issues/133">Issue #133</a>
     */
    @Deprecated
    public static ResponseBody getResponseBodyFromRequest(NextcloudAPI nextcloudAPI, NextcloudRequest request) {
        try {
            final InputStream os = nextcloudAPI.performNetworkRequest(request);
            return ResponseBody.create(null, 0, new BufferedSourceSSO(os));
        } catch (Exception e) {
            Log.e(TAG, "[getResponseBodyFromRequest] encountered a problem", e);
        }
        return ResponseBody.create(null, "");
    }

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
