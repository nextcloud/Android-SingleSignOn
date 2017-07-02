package de.luhmer.owncloud.accountimporter.helper;

import android.accounts.Account;
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
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
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
import java.util.concurrent.ExecutionException;
import java.util.prefs.PreferenceChangeEvent;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

/**
 * Created by david on 29.06.17.
 */

public class NextcloudAPI {

    public NextcloudAPI(Account account, Gson gson) {
        this.account = account;
        this.gson = gson;
    }

    private static final String TAG = NextcloudAPI.class.getCanonicalName();

    private Gson gson = null;
    private IInputStreamService mService = null;
    private boolean mBound = false; // Flag indicating whether we have called bind on the service
    private Account account = null;

    private String getAccountName() {
        return account.name;
    }

    public void start(Context context) {
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

    public <T> T performRequest(final @NonNull Type type, NextcloudRequest request) throws IOException, RemoteException {
        Log.d(TAG, "performRequest() called with: type = [" + type + "], request = [" + request + "]");

        final ParcelFileDescriptor output = performNetworkRequest(request);
        InputStream os = new ParcelFileDescriptor.AutoCloseInputStream(output);

        Reader targetReader = new InputStreamReader(os);
        T result = gson.fromJson(targetReader, type);
        if (result != null) {
            Log.d(TAG, result.toString());
        }
        targetReader.close();

        /*
        byte[] header = new byte[1];
        os.read(header);

        T result = null;
        if(header[0] == 0) { // If not exception
            Reader targetReader = new InputStreamReader(os);
            result = gson.fromJson(targetReader, type);
            if (result != null) {
                Log.d(TAG, result.toString());
            }
            targetReader.close();
        } else {
            try {
                Exception e  = deserializeObjectAndCloseStream(os);
                throw new IOException(e.getMessage(), e.getCause());
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        */
        os.close();
        output.close(); // <-- this is required to halt the TransferThread

        return result;
    }


    private <T> T deserializeObjectAndCloseStream(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        T result = (T) ois.readObject();
        is.close();
        ois.close();
        return result;
    }


    public ParcelFileDescriptor performNetworkRequest(NextcloudRequest request) throws IOException, RemoteException {
        // Log.d(TAG, request.url);
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

        ParcelFileDescriptor output = mService.performNextcloudRequest(input);

        return output;
    }

}
