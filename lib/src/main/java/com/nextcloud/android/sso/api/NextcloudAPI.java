/*
 * Nextcloud SingleSignOn
 *
 * @author David Luhmer
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

package com.nextcloud.android.sso.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

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
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public class NextcloudAPI {

    private static final String TAG = NextcloudAPI.class.getCanonicalName();

    private static final Void NOTHING = getVoidInstance();

    private static Void getVoidInstance() {
        //noinspection unchecked
        final Constructor<Void> constructor = (Constructor<Void>) Void.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Should never happen, but did: unable to instantiate Void");
        }
    }

    private final NetworkRequest networkRequest;
    private Gson gson;

    @Documented
    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface FollowRedirects { }

    public interface ApiConnectedListener {
        void onConnected();
        void onError(Exception ex);
    }

    public NextcloudAPI(@NonNull Context context, @NonNull SingleSignOnAccount account, @NonNull Gson gson, @NonNull ApiConnectedListener callback) {
        this(gson, new AidlNetworkRequest(context, account, callback));
    }

    public NextcloudAPI(Gson gson, NetworkRequest networkRequest) {
        this.gson = gson;
        this.networkRequest = networkRequest;
        new Thread(NextcloudAPI.this.networkRequest::connectApiWithBackoff).start();
    }

    public void stop() {
        gson = null;
        networkRequest.stop();
    }

    /**
     * @deprecated Use {@link #performRequestObservableV2(Type, NextcloudRequest)}
     * @see <a href="https://github.com/nextcloud/Android-SingleSignOn/issues/133">Issue #133</a>
     */
    @Deprecated
    public <T> Observable<T> performRequestObservable(final Type type, final NextcloudRequest request) {
        return Observable.fromPublisher( s -> {
            try {
                s.onNext(performRequest(type, request));
                s.onComplete();
            } catch (Exception e) {
                s.onError(e);
            }
        });
    }

    public <T> Observable<ParsedResponse<T>> performRequestObservableV2(final Type type, final NextcloudRequest request) {
        return Observable.fromPublisher( s -> {
            try {
                final Response response = performNetworkRequestV2(request);
                s.onNext(ParsedResponse.of(convertStreamToTargetEntity(response.getBody(), type), response.getPlainHeaders()));
                s.onComplete();
            } catch (Exception e) {
                s.onError(e);
            }
        });
    }

    /**
     * @deprecated Use {@link #performRequestV2(Type, NextcloudRequest)}
     * @see <a href="https://github.com/nextcloud/Android-SingleSignOn/issues/133">Issue #133</a>
     */
    @Deprecated
    public <T> T performRequest(final @NonNull Type type, NextcloudRequest request) throws Exception {
        Log.d(TAG, "performRequest() called with: type = [" + type + "], request = [" + request + "]");
        return convertStreamToTargetEntity(performNetworkRequest(request), type);
    }

    public <T> T performRequestV2(final @NonNull Type type, NextcloudRequest request) throws Exception {
        Log.d(TAG, "performRequestV2() called with: type = [" + type + "], request = [" + request + "]");
        final Response response = performNetworkRequestV2(request);
        return convertStreamToTargetEntity(response.getBody(), type);
    }

    private <T> T convertStreamToTargetEntity(InputStream inputStream, Type targetEntity) throws IOException {
        final T result;
        try (InputStream os = inputStream;
             Reader targetReader = new InputStreamReader(os)) {
            if (targetEntity != Void.class) {
                result = gson.fromJson(targetReader, targetEntity);
                if (result == null) {
                    if (targetEntity == Object.class) {
                        //noinspection unchecked
                        return (T) NOTHING;
                    } else {
                        throw new IllegalStateException("Could not instantiate \"" +
                                targetEntity.toString() + "\", because response was null.");
                    }
                }
            } else {
                //noinspection unchecked
                result = (T) NOTHING;
            }
        }
        return result;
    }

     /**
      * The InputStreams needs to be closed after reading from it
      *
      * @deprecated Use {@link #performNetworkRequestV2(NextcloudRequest)}
     * @see <a href="https://github.com/nextcloud/Android-SingleSignOn/issues/133">Issue #133</a>
      * @param request {@link NextcloudRequest} request to be executed on server via Files app
      * @return InputStream answer from server as InputStream
      * @throws Exception or {@link SSOException}
     */
     @Deprecated
     public InputStream performNetworkRequest(NextcloudRequest request) throws Exception {
        return networkRequest.performNetworkRequest(request, request.getBodyAsStream());
    }

    /**
     * The InputStreams needs to be closed after reading from it
     *
     * @param request {@link NextcloudRequest} request to be executed on server via Files app
     * @return InputStream answer from server as InputStream
     * @throws Exception or {@link SSOException}
     */
    public Response performNetworkRequestV2(NextcloudRequest request) throws Exception {
        return networkRequest.performNetworkRequestV2(request, request.getBodyAsStream());
    }

    protected Gson getGson() {
        return gson;
    }
}
