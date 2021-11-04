package com.nextcloud.android.sso.sample;


import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @see <a href="https://deck.readthedocs.io/en/latest/API-Nextcloud/">Nextcloud REST API</a>
 */
@SuppressWarnings("unused, SpellCheckingInspection")
public interface OcsAPI {

    @GET("capabilities?format=json")
    Call<OcsResponse<OcsServerInfo>> getServerInfo();

    @GET("users/{search}?format=json")
    Call<OcsResponse<OcsUser>> getUser(@Path("search") String userId);

    /**
     * <p>A generic wrapper for <a href="https://www.open-collaboration-services.org/">OpenCollaborationServices</a> calls.</p>
     * <p>This is a convenience class for API endpoints located at <code>/ocs/…</code> which have an identical wrapping structure. It is usually not used for APIs of 3rd party server apps like <a href="https://deck.readthedocs.io/en/latest/API/">Deck</a> or <a href="https://github.com/nextcloud/notes/blob/master/docs/api/README.md">Notes</a></p>
     *
     * @see <a href="https://github.com/nextcloud/Android-SingleSignOn/issues/401">Request to ship this class by default</a>
     *
     * @param <T> defines the payload type of this {@link OcsResponse}.
     */
    class OcsResponse<T> {
        public OcsWrapper<T> ocs;

        public static class OcsWrapper<T> {
            public OcsMeta meta;
            public T data;
        }

        public static class OcsMeta {
            public String status;
            public int statuscode;
            public String message;
        }
    }

    /**
     * <a href="https://github.com/google/gson"><code>Gson</code></a> maps the payload of the request to this data structure.
     * Attributes must be public or must have public getter & setter.
     *
     * Extend your object mappers by the attributes you are actually using.
     */
    class OcsServerInfo {
        public OcsVersion version;
        public OcsCapabilities capabilities;

        static class OcsVersion {
            /**
             * You can map the <code>JSON</code> attributes to other variable names using {@link SerializedName}.
             * See <a href="https://github.com/google/gson"><code>Gson</code></a>- and <a href="https://square.github.io/retrofit/"><code>Retrofit</code></a>-Documentation for all possibilities.
             */
            @SerializedName("string")
            public String semanticVersion;
        }

        static class OcsCapabilities {
            public Theming theming;

            static class Theming {
                public String name;
            }
        }
    }

    class OcsUser {
        public String displayname;
    }
}
