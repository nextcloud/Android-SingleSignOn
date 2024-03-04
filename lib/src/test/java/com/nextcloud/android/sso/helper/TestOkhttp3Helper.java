/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2023-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.api.EmptyResponse;
import com.nextcloud.android.sso.api.NextcloudAPI;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import okhttp3.ResponseBody;


public final class TestOkhttp3Helper {

    private final String mApiEndpoint = "/index.php/apps/news/api/v1-2/";

    @Mock
    private NextcloudAPI nextcloudApiMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private InputStream objectToInputStream(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Test
    public void testEmptyResponse() throws Exception {
        EmptyResponse emptyResponse = new EmptyResponse();
        InputStream is = objectToInputStream(emptyResponse);
        lenient().when(nextcloudApiMock.performNetworkRequestV2(any())).thenReturn(new com.nextcloud.android.sso.api.Response(is, new ArrayList<>()));

        final var request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "test")
                .build();

        ResponseBody rb = Okhttp3Helper.getResponseBodyFromRequestV2(nextcloudApiMock, request);
        ObjectInputStream ois = new ObjectInputStream(rb.byteStream());
        Object obj = ois.readObject();
        assertEquals(EmptyResponse.class, obj.getClass());
    }

    @Test
    public void testDataResponse() throws Exception {
        String data = "some data";
        InputStream is = objectToInputStream(data);
        lenient().when(nextcloudApiMock.performNetworkRequestV2(any())).thenReturn(new com.nextcloud.android.sso.api.Response(is, new ArrayList<>()));

        final var request = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setUrl(mApiEndpoint + "test")
                .build();

        ResponseBody rb = Okhttp3Helper.getResponseBodyFromRequestV2(nextcloudApiMock, request);
        ObjectInputStream ois = new ObjectInputStream(rb.byteStream());
        Object obj = ois.readObject();
        assertEquals(String.class, obj.getClass());
    }
}
