package com.nextcloud.android.sso.sample;


import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


/**
 * @see <a href="https://deck.readthedocs.io/en/latest/API-Nextcloud/">Nextcloud REST API</a>
 */
public interface OcsAPI {

    @GET("capabilities?format=json")
    Call<OcsResponse<OcsCapabilities>> getCapabilities();

    @GET("users/{search}?format=json")
    Call<OcsResponse<OcsUser>> getUser(@Path("search") String userId);

    /**
     * <p>A generic wrapper for <a href="https://www.open-collaboration-services.org/">OpenCollaborationServices</a> calls.</p>
     * <p>This is needed for API endpoints located at <code>/ocs/...</code>. It is usually not used for APIs of 3rd party server apps like <a href="https://deck.readthedocs.io/en/latest/API/">Deck</a> or <a href="https://github.com/nextcloud/notes/blob/master/docs/api/README.md">Notes</a></p>
     *
     * @param <T> defines the payload of this {@link OcsResponse}.
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

    /*
     * Extend the classes by the attributes you are actually using, for example:</p>
     * <ul>
     *     <li><code>version</code></li>
     *     <li><code>theming</code></li>
     *     <li><code>server_status</code></li>
     *     <li><code>deck</code></li>
     *     <li>…</li>
     * </ul>
     */
    class OcsCapabilities {
        public Theming theming;

        static class Theming {
            public String name;
        }
    }

    /**
     * You can map the node names to other variable names using {@link SerializedName}.
     * See <a href="https://github.com/google/gson">Gson-</a> and <a href="https://square.github.io/retrofit/">Retrofit-</a>Documentation for all possibilities.
     */
    class OcsUser {
        @SerializedName("id")
        public String userId;
        @SerializedName("displayname")
        public String displayName;
    }
}
