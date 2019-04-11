package com.nextcloud.android.sso.api;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.helper.ExponentialBackoff;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.InputStream;

public abstract class NetworkRequest {

    private static final String TAG = NetworkRequest.class.getCanonicalName();

    private SingleSignOnAccount mAccount;
    protected Context mContext;
    protected NextcloudAPI.ApiConnectedListener mCallback;
    protected boolean mDestroyed = false; // Flag indicating if API is destroyed


    protected NetworkRequest(Context context, SingleSignOnAccount account, NextcloudAPI.ApiConnectedListener callback) {
        this.mContext = context;
        this.mAccount = account;
        this.mCallback = callback;
    }


    protected void connect() {
        Log.v(TAG, "Nextcloud Single sign-on connect() called [" + Thread.currentThread().getName() + "]");
        if (mDestroyed) {
            throw new IllegalStateException("API already destroyed! You cannot reuse a stopped API instance");
        }
    }

    protected abstract InputStream performNetworkRequest(NextcloudRequest request, InputStream requestBodyInputStream) throws Exception;

    protected void connectApiWithBackoff() {
        new ExponentialBackoff(1000, 10000, 2, 5, Looper.getMainLooper(), new Runnable() {
            @Override
            public void run() {
            connect();
            }
        }).start();
    }

    protected void stop() {
        mCallback = null;
        mAccount = null;
        mDestroyed = true;
    }


    protected String getAccountName() {
        return mAccount.name;
    }

    protected String getAccountToken() {
        return mAccount.token;
    }

}
