package com.nextcloud.android.sso.model.ocs;

import com.google.gson.annotations.SerializedName;

/**
 * <p>This is a basic implementation for the <code>capabilities</code> endpoint which maps version and theming properties.<br>
 * You can use it directly in combination with {@link OcsResponse} or extend it.</p>
 * <p>Example usage with Retrofit:</p>
 * <pre>
 * {@code
 * @GET("/ocs/v2.php/cloud/capabilities?format=json")
 * Call<OcsResponse<OcsCapabilities>> getCapabilities();
 * }
 * </pre>
 *
 * @see <a href="https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html#capabilities-api">Capabilities API</a>
 */
@SuppressWarnings("unused, SpellCheckingInspection")
public class OcsCapabilitiesResponse {
    public OcsVersion version;
    public OcsCapabilities capabilities;

    public static class OcsVersion {
        public int major;
        public int minor;
        public int macro;
        public String string;
        public String edition;
        public boolean extendedSupport;
    }

    public static class OcsCapabilities {
        public OcsTheming theming;

        public static class OcsTheming {
            public String name;
            public String url;
            public String slogan;
            public String color;
            @SerializedName("color-text")
            public String colorText;
            @SerializedName("color-element")
            public String colorElement;
            @SerializedName("color-element-bright")
            public String colorElementBright;
            @SerializedName("color-element-dark")
            public String colorElementDark;
            public String logo;
            public String background;
            @SerializedName("background-plain")
            public boolean backgroundPlain;
            @SerializedName("background-default")
            public boolean backgroundDefault;
            @SerializedName("logoheader")
            public String logoHeader;
            public String favicon;
        }
    }
}
