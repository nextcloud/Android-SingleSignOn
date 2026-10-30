/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2018 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso;

import androidx.annotation.RestrictTo;

public class Constants {

    // Authenticator related constants

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_SSO_USER_ID = "user_id";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_SSO_TOKEN = "token";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_SSO_SERVER_URL = "server_url";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_SSO_SHARED_PREFERENCE = "single-sign-on";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_NEXTCLOUD_SSO_EXCEPTION = "NextcloudSsoException";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_NEXTCLOUD_SSO = "NextcloudSSO";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_NEXTCLOUD_FILES_ACCOUNT = "NextcloudFilesAccount";


    // Custom Exceptions

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_EXCEPTION_INVALID_TOKEN = "CE_1";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_EXCEPTION_ACCOUNT_NOT_FOUND = "CE_2";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_EXCEPTION_UNSUPPORTED_METHOD = "CE_3";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_EXCEPTION_INVALID_REQUEST_URL = "CE_4";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_EXCEPTION_HTTP_REQUEST_FAILED = "CE_5";

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static final String INTERNAL_EXCEPTION_ACCOUNT_ACCESS_DECLINED = "CE_6";


    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String SSO_USER_ID = INTERNAL_SSO_USER_ID;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String SSO_TOKEN = INTERNAL_SSO_TOKEN;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String SSO_SERVER_URL = INTERNAL_SSO_SERVER_URL;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String SSO_SHARED_PREFERENCE = INTERNAL_SSO_SHARED_PREFERENCE;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String NEXTCLOUD_SSO_EXCEPTION = INTERNAL_NEXTCLOUD_SSO_EXCEPTION;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String NEXTCLOUD_SSO = INTERNAL_NEXTCLOUD_SSO;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String NEXTCLOUD_FILES_ACCOUNT = INTERNAL_NEXTCLOUD_FILES_ACCOUNT;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String EXCEPTION_INVALID_TOKEN = INTERNAL_EXCEPTION_INVALID_TOKEN;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String EXCEPTION_ACCOUNT_NOT_FOUND = INTERNAL_EXCEPTION_ACCOUNT_NOT_FOUND;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String EXCEPTION_UNSUPPORTED_METHOD = INTERNAL_EXCEPTION_UNSUPPORTED_METHOD;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String EXCEPTION_INVALID_REQUEST_URL = INTERNAL_EXCEPTION_INVALID_REQUEST_URL;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String EXCEPTION_HTTP_REQUEST_FAILED = INTERNAL_EXCEPTION_HTTP_REQUEST_FAILED;
    /// @deprecated will be library private in the future
    @Deprecated(forRemoval = true)
    public static final String EXCEPTION_ACCOUNT_ACCESS_DECLINED = INTERNAL_EXCEPTION_ACCOUNT_ACCESS_DECLINED;
}
