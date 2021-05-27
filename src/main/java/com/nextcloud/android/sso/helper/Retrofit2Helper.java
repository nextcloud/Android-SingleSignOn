package com.nextcloud.android.sso.helper;

import androidx.annotation.NonNull;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;

import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 *  Nextcloud SingleSignOn
 *
 *  @author David Luhmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public final class Retrofit2Helper {

    private Retrofit2Helper() { }

    public static <T> Call<T> WrapInCall(final NextcloudAPI nextcloudAPI, final NextcloudRequest nextcloudRequest, 
                                         final Type resType) {
        return new Call<T>() {

            /**
             * Execute synchronous
             */
            @NonNull
            @Override
            public Response<T> execute() {
                try {
                    T body = nextcloudAPI.performRequestV2(resType, nextcloudRequest);
                    return Response.success(body);
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


    /**
     *
     * @param success if <code>true</code>, a Response.success will be returned, otherwise Response.error(520)
     */
    public static Call<Void> wrapVoidCall(final boolean success) {
        return new Call<Void>() {
            @NonNull
            @Override
            public Response<Void> execute() {
                if(success) {
                    return Response.success(null);
                } else {
                    return Response.error(520, emptyResponseBody);
                }
            }

            @Override
            public void enqueue(@NonNull Callback<Void> callback) {
                if(success) {
                    callback.onResponse(this, Response.success(null));
                } else {
                    callback.onResponse(this, Response.error(520, emptyResponseBody));
                }
            }

            @Override
            public boolean isExecuted() {
                return true;
            }

            @Override
            public void cancel() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @NonNull
            @Override
            public Call<Void> clone() {
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
        };

    }

    private final static ResponseBody emptyResponseBody = new ResponseBody() {
        @Override
        public MediaType contentType() {
            return null;
        }

        @Override
        public long contentLength() {
            return 0;
        }

        @Override
        public BufferedSource source() {
            return null;
        }
    };

}
