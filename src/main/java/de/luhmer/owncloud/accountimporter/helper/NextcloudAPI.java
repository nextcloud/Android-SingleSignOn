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
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.PreferenceChangeEvent;

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

    private NextcloudAPI() {

    }

    private static final String TAG = NextcloudAPI.class.getCanonicalName();

    private static final int MSG_CREATE_NEW_ACCOUNT = 3;
    private static final int MSG_REQUEST_NETWORK_REQUEST = 4;
    private static final int MSG_RESPONSE_NETWORK_REQUEST = 5;

    private Gson gson = null;
    private Messenger mService = null; // Messenger for communicating with the service.
    private boolean mBound = false; // Flag indicating whether we have called bind on the service
    private Context context;


    public static final String EDT_USERNAME_STRING = "edt_username";

    public static final String EDT_OWNCLOUDROOTPATH_STRING = "edt_owncloudRootPath";
    private String getAccountName() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String mOc_root_path = mPrefs.getString(EDT_OWNCLOUDROOTPATH_STRING, null);
        String mUsername     = mPrefs.getString(EDT_USERNAME_STRING, null);
        return mUsername + "@" + mOc_root_path.substring(8);
    }



    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public <T> T performRequest(final Type type, Serializable request) {
        Log.d(TAG, type.toString());
        String accountName = getAccountName();


        final boolean stream = true;

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, MSG_REQUEST_NETWORK_REQUEST, 0, 0);
        Bundle b = new Bundle();
        b.putString("account", accountName);  // e.g. david@nextcloud.test.de
        b.putString("token", "test");    // token that the other app received by calling the AccountManager
        b.putSerializable("request", request);
        b.putBoolean("stream", stream);  // Do you want to stream the data?
        msg.setData(b);

        T result = null;

        msg.replyTo = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_RESPONSE_NETWORK_REQUEST) {
                    if(stream) {

                    } else {
                        Exception exception = (Exception) msg.getData().getSerializable("exception");
                        if(exception != null) {
                            exception.printStackTrace();
                        } else {
                            byte[] resultArr = msg.getData().getByteArray("result");
                            Reader targetReader = new InputStreamReader(new ByteArrayInputStream(resultArr));

                            try {
                                T res = gson.fromJson(targetReader, type);
                                Log.d(TAG, res.toString());
                                targetReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        });

        ServerSocket serverSocket = null;
        AsyncTaskHelper.GenericAsyncTaskWithCallable<Object> at;
        if(stream) {
            try {
                serverSocket = new ServerSocket(0);
                int port = serverSocket.getLocalPort();
                b.putInt("port", port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        if(stream) {
            at = (AsyncTaskHelper.GenericAsyncTaskWithCallable<Object>) handleSocket(type, serverSocket).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if(stream) {
            try {
                //at.get();
                result = (T) at.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, result.toString());
        } else {
            // TODO wait for result handler
        }

        return result;

    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private <T> AsyncTaskHelper.GenericAsyncTaskWithCallable<T> handleSocket(final Type type, final ServerSocket serverSocket) {
        return new AsyncTaskHelper.GenericAsyncTaskWithCallable<>(new Callable<T>() {
            @Override
            public T call() throws Exception {
                T result = null;
                try {
                    Socket s = serverSocket.accept();
                    InputStream in = s.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    //Reader reader = new InputStreamReader(in, "UTF-8");
                    result = gson.fromJson(reader, type);
                    Log.d(TAG, result.toString());

                        /*
                        String line;
                        while ((line = r.readLine()) != null) {
                            Log.d(TAG, line);
                        }
                        */
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    serverSocket.close();
                }
                return result;
            }
        });
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
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

}
