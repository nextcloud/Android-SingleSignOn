package com.nextcloud.android.sso.api;

import java.io.Serializable;

/**
 * Use {@link EmptyResponse} as type for call which do not return a response body.
 * Replaced {@link Void} which was previously used for this scenario since <a href="https://github.com/nextcloud/Android-SingleSignOn/issues/541">Issue #541</a>
 */
public final class EmptyResponse implements Serializable {
}
