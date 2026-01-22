/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Desperate Coder <echotodevnull@gmail.com>
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2018-2020 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.AidlNetworkRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import androidx.annotation.NonNull;
import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class Retrofit2Helper {

    private Retrofit2Helper() {
    }

    public static <T> Call<T> WrapInCall(final NextcloudAPI nextcloudAPI,
                                         final NextcloudRequest nextcloudRequest,
                                         final Type resType) {
        return new Call<>() {

            /**
             * Execute synchronous
             */
            @NonNull
            @Override
            public Response<T> execute() {
                try {
                    final var response = nextcloudAPI.performNetworkRequestV2(nextcloudRequest);
                    final T body = nextcloudAPI.convertStreamToTargetEntity(response.getBody(), resType);
                    final var headers = buildHeaders(response.getPlainHeaders());
                    return Response.success(body, headers);

                } catch (NextcloudHttpRequestFailedException e) {
                    return convertExceptionToResponse(e.getStatusCode(), Optional.ofNullable(e.getCause()).orElse(e));

                } catch (Exception e) {
                    return convertExceptionToResponse(900, e);
                }
            }

            /**
             * Execute asynchronous
             */
            @Override
            public void enqueue(@NonNull final Callback<T> callback) {
                new Thread(() -> callback.onResponse(this, execute())).start();
            }

            @Override
            public boolean isExecuted() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public void cancel() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public boolean isCanceled() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @NonNull
            @Override
            public Call<T> clone() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @NonNull
            @Override
            public Request request() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @NonNull
            @Override
            public Timeout timeout() {
                throw new UnsupportedOperationException("Not implemented");
            }

            private Response<T> convertExceptionToResponse(int statusCode, @NonNull Throwable throwable) {
                final var body = ResponseBody.create(null, throwable.toString());
                final var path = Optional.ofNullable(nextcloudRequest.getUrl()).orElse("");

                return Response.error(
                    body,
                    new okhttp3.Response.Builder()
                        .body(body)
                        .code(statusCode)
                        .message(throwable.getMessage() + Arrays.toString(throwable.getStackTrace()))
                        .protocol(Protocol.HTTP_1_1)
                        .request(new Request.Builder().url("http://example.com" + (path.startsWith("/") ? path : "/" + path)).build())
                        .build());
            }
        };
    }

    /**
     * This preserves all distinct header values without combining them.
     *
     * @param plainHeaders List of headers from the response
     * @return Headers object
     */
    public static Headers buildHeaders(List<AidlNetworkRequest.PlainHeader> plainHeaders) {
        if (plainHeaders == null || plainHeaders.isEmpty()) {
            return new Headers.Builder().build();
        }

        final Headers.Builder builder = new Headers.Builder();
        final Set<String> seen = new HashSet<>();

        for (var header : plainHeaders) {
            final String name = header.getName();
            final String value = header.getValue();

            // Create a unique key for name:value combination
            final String key = name.toLowerCase(Locale.US) + ":" + value;

            // Only add if we haven't seen this exact name:value combination before
            if (!seen.contains(key)) {
                builder.add(name, value);
                seen.add(key);
            }
        }

        return builder.build();
    }
}
