/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2021 Desperate Coder <echotodevnull@gmail.com>
 * SPDX-FileCopyrightText: 2018 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.api;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface API {
    @GET("version")
    Observable<String> getRequest();

    @GET("folders")
    Observable<List<String>> getFolders();

    @POST("folders")
    Call<List<String>> postFolder(@Body Map<String, Object> folderMap);

    @PUT("feeds/{feedId}/rename")
    Completable putFeed(@Path("feedId") long feedId, @Body Map<String, String> paramMap);

    @DELETE("feeds/{feedId}")
    Completable deleteFeed(@Path("feedId") long feedId);

    /** ITEMS **/
    @GET("items")
    Call<List<String>> getItems(
            @Query("batchSize") long batchSize,
            @Query("offset") long offset,
            @Query("type") int type,
            @Query("id") long id,
            @Query("getRead") boolean getRead,
            @Query("oldestFirst") boolean oldestFirst
    );

    @GET("items/updated")
    @Streaming
    Observable<ResponseBody> getStreamingUpdatedItems(
            @Query("lastModified") long lastModified,
            @Query("type") int type,
            @Query("id") long id
    );

    @PUT("items/read/multiple")
    Call<EmptyResponse> putMarkItemsRead(@Body String items);

    @PATCH("test")
    Call<EmptyResponse> invalidPATCH();

    @Headers({
        "X-Foo: Bar",
        "X-Ping: Pong"
    })
    @GET("test")
    Call<EmptyResponse> getWithHeader();

    @GET("/test")
    Call<EmptyResponse> getDynamicHeader(@Header("Content-Range") String contentRange);

    @NextcloudAPI.FollowRedirects
    @GET("/test")
    Call<EmptyResponse> getFollowRedirects();

    @FormUrlEncoded
    @POST("/test")
    Call<ResponseBody> postFormUrlEncodedFieldMap(@FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("/test")
    Call<ResponseBody> postFormUrlEncodedField(@Field("name") String name);

    @GET("cloud/capabilities?format=json")
    Call<ResponseBody> getCapabilities(@Query("test") long test);

    @GET("cloud/capabilities?format=json")
    Call<ResponseBody> getCapabilitiesMultiValue(@Query("test") List<Long> test);
}
