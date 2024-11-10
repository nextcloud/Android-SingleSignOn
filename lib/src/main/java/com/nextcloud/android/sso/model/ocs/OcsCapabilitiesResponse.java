/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.model.ocs;

import com.google.gson.annotations.SerializedName;

/**
 * <p>This is a basic implementation for the <code>capabilities</code> endpoint which maps version and theming properties.<br>
 * You can use it directly in combination with {@link OcsResponse} or extend it.</p>
 * <p>Example usage with Retrofit:</p>
 * <pre>
 * {@code
 * @GET("/ocs/v2.php/cloud/capabilities?format=json")
 * Call<OcsResponse < OcsCapabilities>> getCapabilities();
 * }
 * </pre>
 *
 * @see <a href="https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html#capabilities-api">Capabilities API</a>
 */
@SuppressWarnings("unused, SpellCheckingInspection")
public class OcsCapabilitiesResponse {
    public final OcsVersion version;
    public final OcsCapabilities capabilities;

    public OcsCapabilitiesResponse(OcsVersion version, OcsCapabilities capabilities) {
        this.version = version;
        this.capabilities = capabilities;
    }

    public class OcsVersion {
        public final int major;
        public final int minor;
        public final int macro;
        public final String string;
        public final String edition;
        public final boolean extendedSupport;

        public OcsVersion(int major, int minor, int macro, String string, String edition, boolean extendedSupport) {
            this.major = major;
            this.minor = minor;
            this.macro = macro;
            this.string = string;
            this.edition = edition;
            this.extendedSupport = extendedSupport;
        }
    }

    public class OcsCapabilities {
        public final OcsTheming theming;

        public OcsCapabilities(OcsTheming theming) {
            this.theming = theming;
        }

        public class OcsTheming {
            public final String name;
            public final String url;
            public final String slogan;
            public final String color;
            @SerializedName("color-text")
            public final String colorText;
            @SerializedName("color-element")
            public final String colorElement;
            @SerializedName("color-element-bright")
            public final String colorElementBright;
            @SerializedName("color-element-dark")
            public final String colorElementDark;
            public final String logo;
            public final String background;
            @SerializedName("background-plain")
            public final boolean backgroundPlain;
            @SerializedName("background-default")
            public final boolean backgroundDefault;
            @SerializedName("logoheader")
            public final String logoHeader;
            public final String favicon;

            public OcsTheming(String name, String url, String slogan, String color, String colorText, String colorElement, String colorElementBright, String colorElementDark, String logo, String background, boolean backgroundPlain, boolean backgroundDefault, String logoHeader, String favicon) {
                this.name = name;
                this.url = url;
                this.slogan = slogan;
                this.color = color;
                this.colorText = colorText;
                this.colorElement = colorElement;
                this.colorElementBright = colorElementBright;
                this.colorElementDark = colorElementDark;
                this.logo = logo;
                this.background = background;
                this.backgroundPlain = backgroundPlain;
                this.backgroundDefault = backgroundDefault;
                this.logoHeader = logoHeader;
                this.favicon = favicon;
            }
        }
    }
}
