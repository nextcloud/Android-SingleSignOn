/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.sample;

import com.google.gson.annotations.SerializedName;
import com.nextcloud.android.sso.model.ocs.OcsCapabilitiesResponse;
import com.nextcloud.android.sso.model.ocs.OcsResponse;
import com.nextcloud.android.sso.model.ocs.OcsUser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @see <a href="https://deck.readthedocs.io/en/latest/API-Nextcloud/">Nextcloud REST API</a>
 */
public interface OcsAPI {

    @GET("users/{search}?format=json")
    Call<OcsResponse<OcsUser>> getUser(@Path("search") String userId);

    @GET("capabilities?format=json")
    Call<CustomResponse> getServerInfo();

    /**
     * <p>This is for demonstration purposes only. In your apps, you will usually want to use
     * {@link OcsResponse} for requests which target <code>/ocs/…</code> in combination with
     * {@link OcsCapabilitiesResponse} or a subclass.</p>
     *
     * <p><a href="https://github.com/google/gson"><code>Gson</code></a> maps the payload of the request to this data structure.<br>
     * Attributes must be public or must have public getter & setter.</p>
     *
     * <p>Extend your object mappers by the attributes you are actually using.</p>
     */
    class CustomResponse {
        public OcsNode ocs;

        public static class OcsNode {
            public DataNode data;

            public static class DataNode {
                public VersionNode version;
                public CapabilitiesNode capabilities;

                public static class VersionNode {
                    /**
                     * You can map the <code>JSON</code> attributes to other variable names using {@link SerializedName}.
                     * See <a href="https://github.com/google/gson"><code>Gson</code></a>- and <a href="https://square.github.io/retrofit/"><code>Retrofit</code></a>-Documentation for all possibilities.
                     */
                    @SerializedName("string")
                    public String semanticVersion;
                }

                public static class CapabilitiesNode {
                    public ThemingNode theming;

                    public static class ThemingNode {
                        public String name;
                    }
                }
            }
        }
    }
}
