package com.nextcloud.android.sso.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.nextcloud.android.sso.Constants;
import com.nextcloud.android.sso.aidl.IInputStreamService;
import com.nextcloud.android.sso.aidl.IThreadListener;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.aidl.ParcelFileDescriptorUtil;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException;
import com.nextcloud.android.sso.exceptions.NextcloudInvalidRequestUrlException;
import com.nextcloud.android.sso.exceptions.NextcloudUnsupportedMethodException;
import com.nextcloud.android.sso.exceptions.TokenMismatchException;
import com.nextcloud.android.sso.helper.ExponentialBackoff;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

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

public class NextcloudAPI {

    public interface ApiConnectedListener {
        void onConnected();
        void onError(Exception ex);
    }

    public NextcloudAPI(Context context, SingleSignOnAccount account, Gson gson, ApiConnectedListener callback) {
        this.mContext = context;
        this.mAccount = account;
        this.gson = gson;
        this.mCallback = callback;

        connectApiWithBackoff();
    }

    private static final String TAG = NextcloudAPI.class.getCanonicalName();

    private Gson gson;
    private IInputStreamService mService = null;
    private boolean mBound = false; // Flag indicating whether we have called bind on the service
    private boolean mDestroyed = false; // Flag indicating if API is destroyed
    private SingleSignOnAccount mAccount;
    private ApiConnectedListener mCallback;
    private Context mContext;


    private String getAccountName() {
        return mAccount.name;
    }

    private String getAccountToken() {
        return mAccount.token;
    }

    private void connectApiWithBackoff() {
        new ExponentialBackoff(1000, 10000, 2, 5, Looper.getMainLooper(), new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();
    }

    private void connect() {
        if(mDestroyed) {
            throw new IllegalStateException("API already destroyed! You cannot reuse a stopped API instance");
        }

        // Disconnect if connected
        if(mBound) {
            stop();
        }

        try {
            Intent intentService = new Intent();
            intentService.setComponent(new ComponentName("com.nextcloud.client", "com.owncloud.android.services.AccountManagerService"));
            if (!mContext.bindService(intentService, mConnection, Context.BIND_AUTO_CREATE)) {
                Log.d(TAG, "Binding to AccountManagerService returned false");
                throw new IllegalStateException("Binding to AccountManagerService returned false");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "can't bind to AccountManagerService, check permission in Manifest");
            mCallback.onError(e);
        }
    }


    public void stop() {
        gson = null;
        mDestroyed = true;
        mAccount = null;
        mCallback = null;

        // Unbind from the service
        if (mBound) {
            if(mContext != null) {
                mContext.unbindService(mConnection);
            } else {
                Log.e(TAG, "Context was null, cannot unbind nextcloud single sign-on service connection!");
            }
            mBound = false;
            mContext = null;
        }
    }


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "Nextcloud Single sign-on: onServiceConnected");

            mService = IInputStreamService.Stub.asInterface(service);
            mBound = true;
            mCallback.onConnected();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Nextcloud Single sign-on: ServiceDisconnected");
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;

            if(!mDestroyed) {
                connectApiWithBackoff();
            }
        }
    };


    public <T> Observable<T> performRequestObservable(final Type type, final NextcloudRequest request) {
        return Observable.fromPublisher(new Publisher<T>() {
            @Override
            public void subscribe(Subscriber<? super T> s) {
                try {
                    s.onNext((T) performRequest(type, request));
                    s.onComplete();
                } catch (Exception e) {
                    e.printStackTrace();
                    s.onError(e);
                }
            }
        });
    }

    public <T> T performRequest(final @NonNull Type type, NextcloudRequest request) throws Exception {
        Log.d(TAG, "performRequest() called with: type = [" + type + "], request = [" + request + "]");

        InputStream os = performNetworkRequest(request);
        Reader targetReader = new InputStreamReader(os);

        T result = null;
        if (type != Void.class) {
            result = gson.fromJson(targetReader, type);
            if (result != null) {
                Log.d(TAG, result.toString());
            }
        }
        targetReader.close();

        os.close();

        return result;
    }


    public static <T> T deserializeObject(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        T result = (T) ois.readObject();
        return result;
    }


    /**
     * The InputStreams needs to be closed after reading from it
     * @param request
     * @return
     * @throws IOException
     */
    public InputStream performNetworkRequest(NextcloudRequest request) throws Exception {
        InputStream os = null;
        Exception exception = null;
        try {
            ParcelFileDescriptor output = performAidlNetworkRequest(request);
            os = new ParcelFileDescriptor.AutoCloseInputStream(output);
            exception = deserializeObject(os);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Handle Remote Exceptions
        if(exception != null) {
            if(exception.getMessage() != null) {
                switch (exception.getMessage()) {
                    case Constants.EXCEPTION_INVALID_TOKEN:
                        throw new TokenMismatchException();
                    case Constants.EXCEPTION_ACCOUNT_NOT_FOUND:
                        throw new NextcloudFilesAppAccountNotFoundException();
                    case Constants.EXCEPTION_UNSUPPORTED_METHOD:
                        throw new NextcloudUnsupportedMethodException();
                    case Constants.EXCEPTION_INVALID_REQUEST_URL:
                        throw new NextcloudInvalidRequestUrlException(exception.getCause().getMessage());
                    case Constants.EXCEPTION_HTTP_REQUEST_FAILED:
                        int statusCode = Integer.parseInt(exception.getCause().getMessage());
                        throw new NextcloudHttpRequestFailedException(statusCode);
                    default:
                        throw exception;
                }
            }
            throw exception;
        }
        return os;
    }

    /**
     * DO NOT CALL THIS METHOD DIRECTLY - use "performNetworkRequest(...)" instead
     * @param request
     * @return
     * @throws IOException
     */
    private ParcelFileDescriptor performAidlNetworkRequest(NextcloudRequest request) throws IOException, RemoteException {
        // Log.d(TAG, request.url);
        request.setAccountName(getAccountName());
        request.setToken(getAccountToken());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.close();
        baos.close();
        InputStream is = new ByteArrayInputStream(baos.toByteArray());

        ParcelFileDescriptor input = ParcelFileDescriptorUtil.pipeFrom(is,
                new IThreadListener() {
                    @Override
                    public void onThreadFinished(Thread thread) {
                        Log.d(TAG, "copy data from service finished");
                    }
                });

        ParcelFileDescriptor output = mService.performNextcloudRequest(input);

        return output;
    }



    public static <T> T deserializeObjectAndCloseStream(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        T result = (T) ois.readObject();
        is.close();
        ois.close();
        return result;
    }


}
