package com.nextcloud.android.sso.helper;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.NextcloudAPI;

import java.lang.reflect.Type;

import okhttp3.Request;
import okhttp3.ResponseBody;
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

public class Retrofit2Helper {

    public static <T> Call<T> WrapInCall(final NextcloudAPI nextcloudAPI, final NextcloudRequest nextcloudRequest, 
                                         final Type resType) {
        return new Call<T>() {
            @Override
            public Response<T> execute() {
                try {
                    T body = nextcloudAPI.performRequest(resType, nextcloudRequest);
                    return Response.success(body);
                } catch (Exception e) {
                    return Response.error(520, ResponseBody.create(null, e.toString()));
                }
            }

            @Override
            public void enqueue(Callback<T> callback) {
                callback.onResponse(null, execute());
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

            @Override
            public Call<T> clone() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public Request request() {
                throw new UnsupportedOperationException("Not implemented");
            }
        };
    }


    /**
     *
     * @param success if true, a Response.success will be returned, otherwise Response.error(520)
     * @return
     */
    public static Call<Void> WrapVoidCall(final boolean success) {
        return new Call<Void>() {
            @Override
            public Response<Void> execute() {
                if(success) {
                    return Response.success(null);
                } else {
                    return Response.error(520, null);
                }
            }

            @Override
            public void enqueue(Callback callback) {
                if(success) {
                    callback.onResponse(null, Response.success(null));
                } else {
                    callback.onResponse(null, Response.error(520, null));
                }
            }

            @Override
            public boolean isExecuted() {
                return true;
            }

            @Override
            public void cancel() { }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public Call<Void> clone() {
                return null;
            }

            @Override
            public Request request() {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

    }

}
