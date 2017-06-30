package de.luhmer.owncloud.accountimporter.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.PreferenceChangeEvent;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

/**
 * Created by david on 29.06.17.
 */

public class NextcloudAPI {

    private static NextcloudAPI _instance;
    public static NextcloudAPI getInstance() {
        if(_instance == null) {
            _instance = new NextcloudAPI();
        }
        return _instance;
    }

    private NextcloudAPI() { }

    private static final String TAG = NextcloudAPI.class.getCanonicalName();

    private Gson gson = null;
    private IInputStreamService mService = null;
    private boolean mBound = false; // Flag indicating whether we have called bind on the service
    private Context context;


    private static final String EDT_USERNAME_STRING = "edt_username";
    private static final String EDT_OWNCLOUDROOTPATH_STRING = "edt_owncloudRootPath";

    private String getAccountName() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String mOc_root_path = mPrefs.getString(EDT_OWNCLOUDROOTPATH_STRING, null);
        String mUsername     = mPrefs.getString(EDT_USERNAME_STRING, null);
        return mUsername + "@" + mOc_root_path.substring(8);
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void start(Context context) {
        this.context = context;

        try {
            Intent intentService = new Intent();
            intentService.setComponent(new ComponentName("com.nextcloud.client", "com.owncloud.android.services.AccountManagerService"));
            if (context.bindService(intentService, mConnection, Context.BIND_AUTO_CREATE)) {
                //Log.d(TAG, "Binding to AccountManagerService returned true");
            } else {
                Log.d(TAG, "Binding to AccountManagerService returned false");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "can't bind to AccountManagerService, check permission in Manifest");
        }
    }


    public void stop() {
        // Unbind from the service
        if (mBound) {
            context.unbindService(mConnection);
            mBound = false;
        }

        this.context = null;
    }



    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IInputStreamService.Stub.asInterface(service);
            mBound = true;
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
                return performRequest(type, request);
            }
        });
    }

    public <T> T performRequest(final @NonNull Type type, NextcloudRequest request) {
        Log.d(TAG, "performRequest() called with: type = [" + type + "], request = [" + request + "]");

        final ParcelFileDescriptor output = performNetworkRequest(request);
        InputStream os = new ParcelFileDescriptor.AutoCloseInputStream(output);
        Reader targetReader = new InputStreamReader(os);
        T result = gson.fromJson(targetReader, type);
        if(result != null) {
            Log.d(TAG, result.toString());
        }

        try {
            os.close();
            output.close(); // <-- this is required to halt the TransferThread
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }



    public ParcelFileDescriptor performNetworkRequest(NextcloudRequest request) {
        // Log.d(TAG, request.url);
        ParcelFileDescriptor output = null;
        try {
            request.accountName = getAccountName();

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
                            Log.d(TAG, "Test #1: copy to service finished");
                        }
                    });

            output = mService.performNextcloudRequest(input);
        } catch (RemoteException | IOException e) {
            e.printStackTrace();
        }
        return output;
    }


    public interface StreamingInterface {
        void stream(InputStream is);
    }

}
