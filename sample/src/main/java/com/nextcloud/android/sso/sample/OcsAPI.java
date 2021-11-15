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

        static class OcsNode {
            public DataNode data;

            static class DataNode {
                public VersionNode version;
                public CapabilitiesNode capabilities;

                static class VersionNode {
                    /**
                     * You can map the <code>JSON</code> attributes to other variable names using {@link SerializedName}.
                     * See <a href="https://github.com/google/gson"><code>Gson</code></a>- and <a href="https://square.github.io/retrofit/"><code>Retrofit</code></a>-Documentation for all possibilities.
                     */
                    @SerializedName("string")
                    public String semanticVersion;
                }

                static class CapabilitiesNode {
                    public ThemingNode theming;

                    static class ThemingNode {
                        public String name;
                    }
                }
            }
        }
    }
}
