package com.nextcloud.android.sso.api;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nextcloud.android.sso.QueryParam;
import com.nextcloud.android.sso.aidl.NextcloudRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.NextcloudRetrofitApiBuilder;
import retrofit2.Response;

public class TestRetrofitAPI {

    /**
     * Allowed methods:
     * GET, POST, PUT, DELETE
     * <p>
     * Unsupported Methods:
     * PATCH, …
     **/

    private final String mApiEndpoint = "/index.php/apps/news/api/v1-2/";
    private API mApi;

    @Mock
    private NextcloudAPI nextcloudApiMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        lenient().when(nextcloudApiMock.getGson()).thenReturn(new GsonBuilder().create());
        lenient().when(nextcloudApiMock.performRequestObservableV2(any(), any())).thenReturn(Observable.empty());
        lenient().when(nextcloudApiMock.performNetworkRequestV2(any())).thenReturn(new com.nextcloud.android.sso.api.Response(null, null));
        mApi = new NextcloudRetrofitApiBuilder(nextcloudApiMock, mApiEndpoint).create(API.class);
    }

    @Test
    public void getRequest() {
        mApi.getRequest();
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "version")
                .build();
        verify(nextcloudApiMock).performRequestObservableV2(eq(String.class), eq(request));
    }

    @Test
    public void getFolders() {
        mApi.getFolders();

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "folders")
                .build();

        Type type = new TypeToken<List<String>>() {}.getType();
        verify(nextcloudApiMock).performRequestObservableV2(eq(type), eq(request));
    }


    @Test
    public void postFolders() {
        // @POST("folders")
        // Call<List<String>> postFolder(@Body Map<String, Object> folderMap);

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "test");
        try {
            mApi.postFolder(map).execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        String expectedBody = "{\"name\":\"test\"}";
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("POST")
                .setUrl(mApiEndpoint + "folders")
                .setRequestBody(expectedBody)
                .build();

        Type type = new TypeToken<List<String>>() {}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void putFeed() {
        // @PUT("feeds/{feedId}/rename")
        // Completable putFeed(@Path("feedId") long feedId, @Body Map<String, String> paramMap);

        HashMap<String, String> map = new HashMap<>();
        map.put("url", "http://www.cyanogenmod.org/wp-content/themes/cyanogenmod/images/favicon.ico");
        map.put("folderId", "81");
        mApi.putFeed(1, map).subscribe();

        String expectedBody = "{\"url\":\"http://www.cyanogenmod.org/wp-content/themes/cyanogenmod/images/favicon.ico\",\"folderId\":\"81\"}";
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("PUT")
                .setUrl(mApiEndpoint + "feeds/1/rename")
                .setRequestBody(expectedBody)
                .build();

        Type type = new TypeToken<Void>(){}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void deleteFeed() {
        // @DELETE("feeds/{feedId}")
        // Completable deleteFeed(@Path("feedId") long feedId);

        mApi.deleteFeed(1).subscribe();
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("DELETE")
                .setUrl(mApiEndpoint + "feeds/1")
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void getItems() {
        //@GET("items")
        //    Call<List<String>> getItems(
        //    @Query("batchSize") long batchSize,
        //    @Query("offset") long offset,
        //    @Query("type") int type,
        //    @Query("id") long id,
        //    @Query("getRead") boolean getRead,
        //    @Query("oldestFirst") boolean oldestFirst
        //);

        try {
            mApi.getItems(100, 100, 1, 1, true, true).execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        final var params = List.of(
                new QueryParam("batchSize", "100"),
                new QueryParam("offset", "100"),
                new QueryParam("type", "1"),
                new QueryParam("id", "1"),
                new QueryParam("getRead", "true"),
                new QueryParam("oldestFirst", "true")
        );

        final var request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "items")
                .setParameter(params)
                .build();

        Type type = new TypeToken<List<String>>() {}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }




    @Test
    public void getStreamingUpdatedItems() {
        //@GET("items/updated")
        //@Streaming
        //Observable<ResponseBody> getStreamingUpdatedItems(
        //    @Query("lastModified") long lastModified,
        //    @Query("type") int type,
        //    @Query("id") long id
        //);


        mApi.getStreamingUpdatedItems(1000, 1000, 1000).blockingSubscribe();

        final var expectedParams = List.of(
                new QueryParam("lastModified", "1000"),
                new QueryParam("id", "1000"),
                new QueryParam("type", "1000")
        );
        final var request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "items/updated")
                .setParameter(expectedParams)
                .build();

        try {
            verify(nextcloudApiMock).performNetworkRequestV2(eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void putMarkItemsRead() {
        //@PUT("items/read/multiple")
        //Call<Void> putMarkItemsRead(@Body String items);

        try {
            mApi.putMarkItemsRead("[2, 3]").execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        String expectedBody = "\"[2, 3]\"";
        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("PUT")
                .setUrl(mApiEndpoint + "items/read/multiple")
                .setRequestBody(expectedBody)
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test(expected = UnsupportedOperationException.class)
    public void testPatch() throws IOException {
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
    public void testStaticHeaders() {
        //@Headers({
        //    "X-Foo: Bar",
        //    "X-Ping: Pong"
        //})
        //@GET("test")
        //Call<Void> getWithHeader();

        try {
            mApi.getWithHeader().execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        Map<String, List<String>> expectedHeader = new HashMap<>();
        List<String> foo = new ArrayList<>();
        List<String> ping = new ArrayList<>();
        foo.add("Bar");
        ping.add("Pong");
        // HTTP header names are case-insensitive, according to RFC 2616 -> lowercase!
        // lower case happens in @Headers.toMultiMap()
        expectedHeader.put("x-foo", foo);
        expectedHeader.put("x-ping", ping);

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "test")
                .setHeader(expectedHeader)
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDynamicHeaders() {
        //@GET("/test")
        //Call<Void> getDynamicHeader(@Header("Content-Range") String contentRange);

        try {
            mApi.getDynamicHeader("1").execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }


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
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testFollowRedirects() {
        //@NextcloudAPI.FollowRedirects
        //@GET("/test")
        //Call<Void> getFollowRedirects();

        try {
            mApi.getFollowRedirects().execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "test")
                .setFollowRedirects(true)
                .build();

        Type type = new TypeToken<Void>() {}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testFormUrlEncodedFieldMap() {
        final var parameter = Collections.singletonList(new QueryParam("key", "value"));
        try {
            mApi.postFormUrlEncodedFieldMap(new HashMap<>() {{ put(parameter.get(0).key, parameter.get(0).value); }}).execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        final var request = new NextcloudRequest.Builder()
                .setMethod("POST")
                .setUrl(mApiEndpoint + "test")
                .setParameter(parameter)
                .build();

        Type type = new TypeToken<ResponseBody>() {}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFormUrlEncodedField() {
        String name = "myname";
        try {
            mApi.postFormUrlEncodedField(name).execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        final var parameters = Collections.singleton(new QueryParam("name", name));

        final var request = new NextcloudRequest.Builder()
                .setMethod("POST")
                .setUrl(mApiEndpoint + "test")
                .setParameter(parameters)
                .build();

        Type type = new TypeToken<ResponseBody>() {}.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testQueryParamInUrl() {
        try {
            mApi.getCapabilities(1).execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        List<QueryParam> params = new ArrayList<>();
        params.add(new QueryParam("format", "json"));
        params.add(new QueryParam("test", "1"));

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "cloud/capabilities")
                .setParameter(params)
                .build();

        Type type = new TypeToken<ResponseBody>() {
        }.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testQueryParamInUrlMultiValueForKey() {
        try {
            mApi.getCapabilitiesMultiValue(Arrays.asList(1L, 3L)).execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        List<QueryParam> params = new ArrayList<>();
        params.add(new QueryParam("format", "json"));
        params.add(new QueryParam("test", "1"));
        params.add(new QueryParam("test", "3"));

        NextcloudRequest request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "cloud/capabilities")
                .setParameter(params)
                .build();

        Type type = new TypeToken<ResponseBody>() {
        }.getType();
        try {
            verify(nextcloudApiMock).performRequestV2(eq(type), eq(request));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testExecute() {
        try {
            mApi.getFollowRedirects().execute();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testEnqueue() throws InterruptedException {

        Callback callback = mock(Callback.class);

        Thread testThread = Thread.currentThread();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            assertNotEquals(testThread, Thread.currentThread());
            countDownLatch.countDown();
            return null;
        }).when(callback).onResponse(any(Call.class), any(Response.class));
        mApi.getFollowRedirects().enqueue(callback);

        // Wait for callback to be done
        boolean successful = countDownLatch.await(1, TimeUnit.SECONDS);
        assertTrue(successful);
    }

}
