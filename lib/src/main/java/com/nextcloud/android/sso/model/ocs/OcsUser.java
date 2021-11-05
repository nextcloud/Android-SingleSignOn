package com.nextcloud.android.sso.model.ocs;

import com.google.gson.annotations.SerializedName;

/**
 * <p>This is a basic implementation for the <code>users</code> endpoint which maps often required properties.<br>
 * You can use it directly in combination with {@link OcsResponse} or extend it.</p>
 * <p>Example usage with Retrofit:</p>
 * <pre>
 * {@code
 * @GET("/ocs/v2.php/cloud/users/{search}?format=json")
 * Call<OcsResponse<OcsUser>> getUser(@Path("search") String userId);
 * }
 * </pre>
 * @see <a href="https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html#user-metadata">User API</a>
 */
@SuppressWarnings("SpellCheckingInspection")
public class OcsUser {
    public boolean enabled;
    @SerializedName("id")
    public String userId;
    public long lastLogin;
    public OcsQuota quota;
    public String email;
    @SerializedName("displayname")
    public String displayName;
    public String phone;
    public String address;
    public String website;
    public String twitter;
    public String[] groups;
    public String language;
    public String locale;

    public static class OcsQuota {
        public long free;
        public long used;
        public long total;
    }
}
