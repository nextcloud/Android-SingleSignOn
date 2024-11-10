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
 * <p>This is a basic implementation for the <code>users</code> endpoint which maps often required properties.<br>
 * You can use it directly in combination with {@link OcsResponse} or extend it.</p>
 * <p>Example usage with Retrofit:</p>
 * <pre>
 * {@code
 * @GET("/ocs/v2.php/cloud/users/{search}?format=json")
 * Call<OcsResponse < OcsUser>> getUser(@Path("search") String userId);
 * }
 * </pre>
 * @see <a href="https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html#user-metadata">User API</a>
 */
@SuppressWarnings("SpellCheckingInspection")
public class OcsUser {
    public final boolean enabled;
    @SerializedName("id")
    public final String userId;
    public final long lastLogin;
    public final OcsQuota quota;
    public final String email;
    @SerializedName("displayname")
    public final String displayName;
    public final String phone;
    public final String address;
    public final String website;
    public final String twitter;
    public final String[] groups;
    public final String language;
    public final String locale;

    public OcsUser(boolean enabled, String userId, long lastLogin, OcsQuota quota, String email, String displayName, String phone, String address, String website, String twitter, String[] groups, String language, String locale) {
        this.enabled = enabled;
        this.userId = userId;
        this.lastLogin = lastLogin;
        this.quota = quota;
        this.email = email;
        this.displayName = displayName;
        this.phone = phone;
        this.address = address;
        this.website = website;
        this.twitter = twitter;
        this.groups = groups;
        this.language = language;
        this.locale = locale;
    }

    public class OcsQuota {
        public final long free;
        public final long used;
        public final long total;

        public OcsQuota(long free, long used, long total) {
            this.free = free;
            this.used = used;
            this.total = total;
        }
    }
}
