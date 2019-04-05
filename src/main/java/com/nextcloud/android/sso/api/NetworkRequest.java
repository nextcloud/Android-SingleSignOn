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
    Context mContext;
    NextcloudAPI.ApiConnectedListener mCallback;
    boolean mDestroyed = false; // Flag indicating if API is destroyed


    protected NetworkRequest(Context context, SingleSignOnAccount account, NextcloudAPI.ApiConnectedListener callback) {
        this.mContext = context;
        this.mAccount = account;
        this.mCallback = callback;
    }


    void connect() {
        Log.v(TAG, "Nextcloud Single sign-on connect() called [" + Thread.currentThread().getName() + "]");
        if (mDestroyed) {
            throw new IllegalStateException("API already destroyed! You cannot reuse a stopped API instance");
        }
    }

    protected abstract InputStream performNetworkRequest(NextcloudRequest request, InputStream requestBodyInputStream) throws Exception;

    void connectApiWithBackoff() {
        new ExponentialBackoff(1000, 10000, 2, 5, Looper.getMainLooper(), new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();
    }

    void stop() {
        mCallback = null;
        mAccount = null;
        mDestroyed = true;
    }


    String getAccountName() {
        return mAccount.name;
    }

    String getAccountToken() {
        return mAccount.token;
    }

}
