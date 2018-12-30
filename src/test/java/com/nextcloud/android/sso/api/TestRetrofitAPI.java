package com.nextcloud.android.sso.api;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nextcloud.android.sso.aidl.NextcloudRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import retrofit2.Call;
import retrofit2.NextcloudRetrofitApiBuilder;
import retrofit2.http.GET;
import retrofit2.http.Header;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestRetrofitAPI {

    /**
     Allowed methods:
     GET, POST, PUT, DELETE

     Unsupported Methods:
     PATCH, ...
     **/

    private final String mApiEndpoint = "/index.php/apps/news/api/v1-2/";
    private API mApi;

    @Mock
    private NextcloudAPI nextcloudApiMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() {
        when(nextcloudApiMock.getGson()).thenReturn(new GsonBuilder().create());
        mApi = new NextcloudRetrofitApiBuilder(nextcloudApiMock, mApiEndpoint).create(API.class);
    }

    @Test
    public void getRequest() {
        mApi.getRequest();
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "version")
                .build();
        verify(nextcloudApiMock).performRequestObservable(eq(String.class), eq(request));
    }

    @Test
    public void getFolders() {
        mApi.getFolders();

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "folders")
                .build();

        Type type = new TypeToken<List<String>>() {}.getType();
        verify(nextcloudApiMock).performRequestObservable(eq(type), eq(request));
    }


    @Test
    public void postFolders() throws Exception {
        // @POST("folders")
        // Call<List<String>> postFolder(@Body Map<String, Object> folderMap);

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "test");
        mApi.postFolder(map).execute();

        String expectedBody = "{\"name\":\"test\"}";
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("POST")
                .setUrl(mApiEndpoint + "folders")
                .setRequestBody(expectedBody)
                .build();

        Type type = new TypeToken<List<String>>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
    }

    @Test
    public void putFeed() throws Exception {
        // @PUT("feeds/{feedId}/rename")
        // Completable putFeed(@Path("feedId") long feedId, @Body Map<String, String> paramMap);

        HashMap<String, String> map = new HashMap<>();
        map.put("url", "http://www.cyanogenmod.org/wp-content/themes/cyanogenmod/images/favicon.ico");
        map.put("folderId", "81");
        mApi.putFeed(1, map);

        String expectedBody = "{\"url\":\"http://www.cyanogenmod.org/wp-content/themes/cyanogenmod/images/favicon.ico\",\"folderId\":\"81\"}";
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("PUT")
                .setUrl(mApiEndpoint + "feeds/1/rename")
                .setRequestBody(expectedBody)
                .build();

        Type type = new TypeToken<Completable>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
    }


    @Test
    public void deleteFeed() throws Exception {
        // @DELETE("feeds/{feedId}")
        // Completable deleteFeed(@Path("feedId") long feedId);

        mApi.deleteFeed(1);
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("DELETE")
                .setUrl(mApiEndpoint + "feeds/1")
                .build();

        Type type = new TypeToken<Completable>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
    }


    @Test
    public void getItems() throws Exception {
        //@GET("items")
        //    Call<List<String>> getItems(
        //    @Query("batchSize") long batchSize,
        //    @Query("offset") long offset,
        //    @Query("type") int type,
        //    @Query("id") long id,
        //    @Query("getRead") boolean getRead,
        //    @Query("oldestFirst") boolean oldestFirst
        //);

        mApi.getItems(100, 100, 1, 1, true, true).execute();
        HashMap<String, String> params = new HashMap<>();
        params.put("batchSize", "100");
        params.put("offset", "100");
        params.put("type", "1");
        params.put("id", "1");
        params.put("getRead", "true");
        params.put("oldestFirst", "true");

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "items")
                .setParameter(params)
                .build();

        Type type = new TypeToken<List<String>>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
    }




    @Test
    public void getStreamingUpdatedItems() throws Exception {
        //@GET("items/updated")
        //@Streaming
        //Observable<ResponseBody> getStreamingUpdatedItems(
        //    @Query("lastModified") long lastModified,
        //    @Query("type") int type,
        //    @Query("id") long id
        //);


        mApi.getStreamingUpdatedItems(1000, 1000, 1000);

        HashMap<String, String> expectedParams = new HashMap<>();
        expectedParams.put("lastModified", "1000");
        expectedParams.put("id", "1000");
        expectedParams.put("type", "1000");
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "items/updated")
                .setParameter(expectedParams)
                .build();

        verify(nextcloudApiMock).performNetworkRequest(eq(request));
    }


    @Test
    public void putMarkItemsRead() throws Exception {
        //@PUT("items/read/multiple")
        //Call<Void> putMarkItemsRead(@Body String items);

        mApi.putMarkItemsRead("[2, 3]").execute();

        String expectedBody = "\"[2, 3]\"";
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("PUT")
                .setUrl(mApiEndpoint + "items/read/multiple")
                .setRequestBody(expectedBody)
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
    }


    @Test(expected = UnsupportedOperationException.class)
    public void testPatch() throws Exception {
        //@PATCH("test")
        //Call<Void> invalidPATCH();


        mApi.invalidPATCH().execute();

        /*
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("PATCH")
                .setUrl(mApiEndpoint + "test")
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
        */
    }

    @Test
    public void testStaticHeaders() throws Exception {
        //@Headers({
        //    "X-Foo: Bar",
        //    "X-Ping: Pong"
        //})
        //@GET("test")
        //Call<Void> getWithHeader();

        mApi.getWithHeader().execute();

        Map<String, List<String>> expectedHeader = new HashMap<>();
        List<String> foo = new ArrayList<>();
        List<String> ping = new ArrayList<>();
        foo.add("Bar");
        ping.add("Pong");
        expectedHeader.put("X-Foo", foo);
        expectedHeader.put("X-Ping", ping);

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "test")
                .setHeader(expectedHeader)
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
    }

    @Test
    public void testDynamicHeaders() throws Exception {
        //@GET("/test")
        //Call<Void> getDynamicHeader(@Header("Content-Range") String contentRange);

        mApi.getDynamicHeader("1").execute();


        Map<String, List<String>> expectedHeader = new HashMap<>();
        List<String> contentRange = new ArrayList<>();
        contentRange.add("1");
        expectedHeader.put("Content-Range", contentRange);

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "test")
                .setHeader(expectedHeader)
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
    }


    @Test
    public void testFollowRedirects() throws Exception {
        //@NextcloudAPI.FollowRedirects
        //@GET("/test")
        //Call<Void> getFollowRedirects();

        mApi.getFollowRedirects().execute();

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "test")
                .setFollowRedirects(true)
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        verify(nextcloudApiMock).performRequest(eq(type), eq(request));
    }
}
