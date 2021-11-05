package com.nextcloud.android.sso.model.ocs;

import com.google.gson.annotations.SerializedName;

/**
 * <p>A generic wrapper for <a href="https://www.open-collaboration-services.org/">OpenCollaborationServices</a> responses.</p>
 * <p>This is a convenience class for API endpoints located at <code>/ocs/…</code> which all have an identical wrapping structure.<br>
 * It is usually <strong>not</strong> used in APIs of 3rd party server apps like <a href="https://deck.readthedocs.io/en/latest/API/">Deck</a> or <a href="https://github.com/nextcloud/notes/blob/master/docs/api/README.md">Notes</a></p>
 * <p>Example usage with Retrofit:</p>
 * <pre>
 * {@code
 * @GET("/ocs/v2.php/cloud/capabilities?format=json")
 * Call<OcsResponse<OcsCapabilitiesResponse>> getCapabilities();
 * }
 * </pre>
 *
 * @param <T> defines the payload type of this {@link OcsResponse}.
 */
@SuppressWarnings("unused, SpellCheckingInspection")
public class OcsResponse<T> {
    public OcsWrapper<T> ocs;

    public static class OcsWrapper<T> {
        public OcsMeta meta;
        public T data;

        public static class OcsMeta {
            public String status;
            @SerializedName("statuscode")
            public int statusCode;
            public String message;
        }
    }
}