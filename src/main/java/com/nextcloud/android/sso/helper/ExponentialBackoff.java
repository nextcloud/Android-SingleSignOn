/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nextcloud.android.sso.helper;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.security.InvalidParameterException;


/** The implementation of exponential backoff with jitter applied. */
public class ExponentialBackoff {

    private static final String TAG = ExponentialBackoff.class.getCanonicalName();

    private int mRetryCounter;
    private final long mStartDelayMs;
    private final long mMaximumDelayMs;
    private long mCurrentDelayMs;
    private final int mMaxRetries;
    private final int mMultiplier;
    private final Runnable mRunnable;
    private final Runnable mFailedCallback;
    private final Handler mHandler;

    /**
     * Implementation of Handler methods, Adapter for testing (can't spy on final methods).
     */
    private HandlerAdapter mHandlerAdapter = new HandlerAdapter() {
        @Override
        public boolean postDelayed(Runnable runnable, long delayMillis) {
            return mHandler.postDelayed(runnable, delayMillis);
        }
        @Override
        public void removeCallbacks(Runnable runnable) {
            mHandler.removeCallbacks(runnable);
        }
    };

    /**
     * Need to spy final methods for testing.
     */
    public interface HandlerAdapter {
        @SuppressWarnings("UnusedReturnValue")
        boolean postDelayed(Runnable runnable, long delayMillis);
        void removeCallbacks(Runnable runnable);
    }

    public ExponentialBackoff(
            long initialDelayMs,
            long maximumDelayMs,
            int multiplier,
            int maxRetries,
            @NonNull Looper looper,
            @NonNull Runnable runnable,
            @NonNull Runnable onErrorRunnable) {
        this(initialDelayMs, maximumDelayMs, multiplier, maxRetries, new Handler(looper), runnable, onErrorRunnable);
    }

    private ExponentialBackoff(
            long initialDelayMs,
            long maximumDelayMs,
            int multiplier,
            int maxRetries,
            @NonNull Handler handler,
            @NonNull Runnable runnable,
            @NonNull Runnable onErrorRunnable) {
        mRetryCounter = 0;
        mStartDelayMs = initialDelayMs;
        mMaximumDelayMs = maximumDelayMs;
        mMultiplier = multiplier;
        mMaxRetries = maxRetries;
        mHandler = handler;
        mRunnable = new WrapperRunnable(runnable);
        mFailedCallback = onErrorRunnable;

        if(initialDelayMs <= 0) {
            throw new InvalidParameterException("initialDelayMs should not be less or equal to 0");
        }
    }

    /** Starts the backoff, the runnable will be executed after {@link #mStartDelayMs}. */
    public void start() {
        mRetryCounter = 0;
        mHandlerAdapter.removeCallbacks(mRunnable);
        mHandlerAdapter.postDelayed(mRunnable, 0);
    }

    /** Stops the backoff, all pending messages will be removed from the message queue. */
    public void stop() {
        mRetryCounter = 0;
        mHandlerAdapter.removeCallbacks(mRunnable);

    }

    /** Should call when the retry action has failed and we want to retry after a longer delay. */
    private void notifyFailed(Exception ex) {
        Log.d(TAG, "[notifyFailed] Error: [" + ex.getMessage() + "]");
        if(mRetryCounter > mMaxRetries) {
            Log.d(TAG, "[notifyFailed] Retries exceeded, ending now");
            stop();
            mFailedCallback.run();
        } else {
            mRetryCounter++;
            long temp = Math.min(
                    mMaximumDelayMs, (long) (mStartDelayMs * Math.pow(mMultiplier, mRetryCounter)));
            mCurrentDelayMs = (long) (((1 + Math.random()) / 2) * temp);
            Log.d(TAG, "[notifyFailed] retrying in: [" + mCurrentDelayMs + "ms]");
            mHandlerAdapter.removeCallbacks(mRunnable);
            mHandlerAdapter.postDelayed(mRunnable, mCurrentDelayMs);
        }
    }

    class WrapperRunnable implements Runnable {

        private final Runnable runnable;

        WrapperRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } catch (Exception ex) {
                notifyFailed(ex);
            }
        }
    }

    /** Returns the delay for the most recently posted message. */
    public long getCurrentDelay() {
        return mCurrentDelayMs;
    }

    @VisibleForTesting
    public void setHandlerAdapter(HandlerAdapter a) {
        mHandlerAdapter  = a;
    }
}