/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AidlNetworkRequestTest {

    private static final String ACCOUNT_TYPE = "nextcloud";

    private Context context;
    private NextcloudAPI.ApiConnectedListener callback;
    private AidlNetworkRequest request;

    @Before
    public void setUp() {
        context = mock(Context.class);
        callback = mock(NextcloudAPI.ApiConnectedListener.class);
        final var account = new SingleSignOnAccount("name", "userId", "token", "https://example.com", ACCOUNT_TYPE);
        request = new AidlNetworkRequest(context, account, callback);
    }

    private ServiceConnection connectAndCaptureServiceConnection() {
        when(context.bindService(any(Intent.class), any(ServiceConnection.class), anyInt())).thenReturn(true);
        request.connect(ACCOUNT_TYPE);

        final var connectionCaptor = ArgumentCaptor.forClass(ServiceConnection.class);
        verify(context).bindService(any(Intent.class), connectionCaptor.capture(), anyInt());
        return connectionCaptor.getValue();
    }

    @Test
    public void onServiceConnectedBeforeCloseInvokesCallback() {
        final var connection = connectAndCaptureServiceConnection();

        connection.onServiceConnected(new ComponentName("com.nextcloud.client", "AccountManagerService"), mock(IBinder.class));

        verify(callback).onConnected();
    }

    @Test
    public void onServiceConnectedAfterCloseDoesNotThrowAndDoesNotInvokeCallback() {
        final var connection = connectAndCaptureServiceConnection();

        request.close();
        connection.onServiceConnected(new ComponentName("com.nextcloud.client", "AccountManagerService"), mock(IBinder.class));

        verify(callback, never()).onConnected();
    }

    @Test
    public void closeBeforeServiceConnectedUnbindsServiceConnection() {
        final var connection = connectAndCaptureServiceConnection();

        request.close();

        verify(context).unbindService(connection);
    }
}
