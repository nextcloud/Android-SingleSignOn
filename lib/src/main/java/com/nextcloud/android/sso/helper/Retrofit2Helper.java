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

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.EmptyResponse;
import com.nextcloud.android.sso.api.AidlNetworkRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class Retrofit2Helper {

    private Retrofit2Helper() { }

    public static <T> Call<T> WrapInCall(final NextcloudAPI nextcloudAPI, final NextcloudRequest nextcloudRequest, 
                                         final Type resType) {
        return new Call<>() {

            /**
             * Execute synchronous
             */
            @NonNull
            @Override
            public Response<T> execute() {
                try {
                    com.nextcloud.android.sso.api.Response response = nextcloudAPI.performNetworkRequestV2(nextcloudRequest);

                    T body = nextcloudAPI.convertStreamToTargetEntity(response.getBody(), resType);
                    Map<String, String> headerMap = new HashMap<>();
                    ArrayList<AidlNetworkRequest.PlainHeader> plainHeaders = response.getPlainHeaders();
                    if (plainHeaders != null) {
                        for (AidlNetworkRequest.PlainHeader header : plainHeaders) {
                            headerMap.put(header.getName(), header.getValue());
                        }
                    }
                    return Response.success(body, Headers.of(headerMap));
                } catch (NextcloudHttpRequestFailedException e) {
                    final Throwable cause = e.getCause();
                    return convertExceptionToResponse(e.getStatusCode(), cause == null ? e.getMessage() : cause.getMessage());
                } catch (Exception e) {
                    return convertExceptionToResponse(520, e.toString());
                }
            }

            /**
             * Execute asynchronous
             */
            @Override
            public void enqueue(@NonNull final Callback<T> callback) {
                final Call<T> call = this;
                new Thread(() -> callback.onResponse(call, execute())).start();
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

            private Response<T> convertExceptionToResponse(int statusCode, String errorMessage) {
                ResponseBody body = ResponseBody.create(null, errorMessage);
                return Response.error(
                        body,
                        new okhttp3.Response.Builder()
                                .body(body)
                                .code(statusCode)
                                .message(errorMessage)
                                .protocol(Protocol.HTTP_1_1)
                                .request(new Request.Builder().url("http://localhost/" + nextcloudRequest.getUrl()).build())
                                .build());
            }
        };
    }
}
