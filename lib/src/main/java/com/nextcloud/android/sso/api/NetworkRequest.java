package com.nextcloud.android.sso.api;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

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

    protected NetworkRequest(@NonNull Context context, @NonNull SingleSignOnAccount account, @NonNull NextcloudAPI.ApiConnectedListener callback) {
        this.mContext = context;
        this.mAccount = account;
        this.mCallback = callback;
    }

    protected void connect(String type) {
        Log.d(TAG, "[connect] connect() called [" + Thread.currentThread().getName() + "] Account-Type: [" + type + "]");
        if (mDestroyed) {
            throw new IllegalStateException("API already destroyed! You cannot reuse a stopped API instance");
        }
    }

    protected abstract Response performNetworkRequestV2(NextcloudRequest request, InputStream requestBodyInputStream) throws Exception;

    protected void connectApiWithBackoff() {
        Log.d(TAG, "[connectApiWithBackoff] connectApiWithBackoff() called from Thread: [" + Thread.currentThread().getName() + "]");
        new ExponentialBackoff(1_000, 5_000, 2, 5, Looper.getMainLooper(), () -> {
            Log.d(TAG, "[connectApiWithBackoff] trying to connect..");
            connect(mAccount.type);
        }, () -> {
            Log.e(TAG, "Unable to recover API");
            stop();
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
