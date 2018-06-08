package de.luhmer.owncloud.accountimporter.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

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

    public NextcloudAPI(SingleSignOnAccount account, Gson gson) {
        this.mAccount = account;
        this.gson = gson;
    }

    private static final String TAG = NextcloudAPI.class.getCanonicalName();

    private Gson gson;
    private IInputStreamService mService = null;
    private boolean mBound = false; // Flag indicating whether we have called bind on the service
    private SingleSignOnAccount mAccount;
    private ApiConnectedListener mCallback;

    private String getAccountName() {
        return mAccount.name;
    }

    private String getAccountToken() {
        return mAccount.password;
    }

    public void start(Context context, ApiConnectedListener callback) {
        this.mCallback = callback;

        // Disconnect if connected
        if(mBound) {
            stop(context);
        }

        try {
            Intent intentService = new Intent();
            intentService.setComponent(new ComponentName("com.nextcloud.client", "com.owncloud.android.services.AccountManagerService"));
            if (!context.bindService(intentService, mConnection, Context.BIND_AUTO_CREATE)) {
                Log.d(TAG, "Binding to AccountManagerService returned false");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "can't bind to AccountManagerService, check permission in Manifest");
            callback.onError(e);
        }
    }


    public void stop(Context context) {
        // Unbind from the service
        if (mBound) {
            context.unbindService(mConnection);
            mBound = false;
        }
    }


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IInputStreamService.Stub.asInterface(service);
            mBound = true;
            mCallback.onConnected();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };


    public <T> Observable<T> performRequestObservable(final Type type, final NextcloudRequest request) {
        return Observable.fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                T result = performRequest(type, request);
                Log.d(TAG, "Wrapping result object in Observable: " + result);
                return result;
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
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(exception != null) {
            throw exception;
        }
        return os;
    }

    private ParcelFileDescriptor performAidlNetworkRequest(NextcloudRequest request) throws IOException, RemoteException {
        // Log.d(TAG, request.url);
        request.accountName = getAccountName();
        request.token = getAccountToken();


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
