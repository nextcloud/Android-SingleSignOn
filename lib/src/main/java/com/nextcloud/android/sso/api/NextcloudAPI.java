/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2017-2024 David Luhmer <david-dev@live.de>
 * SPDX-FileCopyrightText: 2023 Desperate Coder <echotodevnull@gmail.com>
 * SPDX-FileCopyrightText: 2023 sim <git@sgougeon.fr>
 * SPDX-FileCopyrightText: 2021-2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018-2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.exceptions.SSOException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public class NextcloudAPI implements AutoCloseable {

    private static final String TAG = NextcloudAPI.class.getCanonicalName();

    private static final EmptyResponse EMPTY_RESPONSE = new EmptyResponse();

    private final NetworkRequest networkRequest;
    private Gson gson;

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface FollowRedirects {
    }

    public interface ApiConnectedListener {
        default void onConnected() {
            Log.i(TAG, "Single Sign On API successfully connected.");
        }

        void onError(Exception e);
    }

    public NextcloudAPI(@NonNull Context context, @NonNull SingleSignOnAccount account, @NonNull Gson gson) {
        this(gson, new AidlNetworkRequest(context, account, Throwable::printStackTrace));
    }

    public NextcloudAPI(@NonNull Context context, @NonNull SingleSignOnAccount account, @NonNull Gson gson, @NonNull ApiConnectedListener callback) {
        this(gson, new AidlNetworkRequest(context, account, callback));
    }

    public NextcloudAPI(Gson gson, NetworkRequest networkRequest) {
        this.gson = gson;
        this.networkRequest = networkRequest;
        new Thread(NextcloudAPI.this.networkRequest::connectApiWithBackoff).start();
    }

    /**
     * <blockquote>
     * If you need to make multiple calls, keep the {@link NextcloudAPI} open as long as you
     * can. This way the services will stay active and the connection between the
     * files app and your app is already established when you make subsequent requests.
     * Otherwise you'll have to bind to the service again and again for each request.
     * <cite><a href="https://github.com/nextcloud/Android-SingleSignOn/issues/120#issuecomment-540069990">Source</a></cite>
     * </blockquote>
     *
     * <p>A good place <em>depending on your actual implementation</em> might be {@link Activity#onStop()}.</p>
     */
    @Override
    @SuppressWarnings("JavadocReference")
    public void close() {
        gson = null;
        networkRequest.close();
    }

    public <T> Observable<ParsedResponse<T>> performRequestObservableV2(final Type type, final NextcloudRequest request) {
        ensureTypeNotVoid(type);
        return Observable.fromPublisher(s -> {
            try {
                final Response response = performNetworkRequestV2(request);
                s.onNext(ParsedResponse.of(convertStreamToTargetEntity(response.getBody(), type), response.getPlainHeaders()));
                s.onComplete();
            } catch (Exception e) {
                s.onError(e);
            }
        });
    }

    public <T> io.reactivex.rxjava3.core.Observable<ParsedResponse<T>> performRequestObservableV3(final Type type, final NextcloudRequest request) {
        ensureTypeNotVoid(type);
        return io.reactivex.rxjava3.core.Observable.fromPublisher(s -> {
            try {
                final Response response = performNetworkRequestV2(request);
                s.onNext(ParsedResponse.of(convertStreamToTargetEntity(response.getBody(), type), response.getPlainHeaders()));
                s.onComplete();
            } catch (Exception e) {
                s.onError(e);
            }
        });
    }

    public <T> T performRequestV2(final @NonNull Type type, NextcloudRequest request) throws Exception {
        Log.d(TAG, "performRequestV2() called with: type = [" + type + "], request = [" + request + "]");
        ensureTypeNotVoid(type);
        final Response response = performNetworkRequestV2(request);
        return convertStreamToTargetEntity(response.getBody(), type);
    }

    public <T> T convertStreamToTargetEntity(InputStream inputStream, Type targetEntity) throws IOException {
        ensureTypeNotVoid(targetEntity);

        final T result;
        try (InputStream os = inputStream;
             Reader targetReader = new InputStreamReader(os)) {
            if (targetEntity == EmptyResponse.class) {
                //noinspection unchecked
                result = (T) EMPTY_RESPONSE;
            } else {
                result = gson.fromJson(targetReader, targetEntity);
                if (result == null) {
                    if (targetEntity == Object.class) {
                        //noinspection unchecked
                        return (T) EMPTY_RESPONSE;
                    } else {
                        throw new IllegalStateException("Could not instantiate \"" +
                                targetEntity + "\", because response was null.");
                    }
                }
            }
        }
        return result;
    }

    /**
     * The InputStreams needs to be closed after reading from it
     *
     * @param request {@link NextcloudRequest} request to be executed on server via Files app
     * @return InputStream answer from server as InputStream
     * @throws Exception or {@link SSOException}
     */
    public Response performNetworkRequestV2(@NonNull NextcloudRequest request) throws Exception {
        return networkRequest.performNetworkRequestV2(request, request.getBodyAsStream());
    }

    private void ensureTypeNotVoid(final @NonNull Type type) {
        if (type == Void.class) {
            throw new IllegalArgumentException(Void.class.getSimpleName() + " is not supported. Use " + EmptyResponse.class.getSimpleName() + " for calls without a response body. See also: https://github.com/nextcloud/Android-SingleSignOn/issues/541");
        }
    }

    protected Gson getGson() {
        return gson;
    }
}
