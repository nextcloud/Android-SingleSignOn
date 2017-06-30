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

    private NextcloudAPI() {

    }

    private static final String TAG = NextcloudAPI.class.getCanonicalName();

    private static final int MSG_CREATE_NEW_ACCOUNT = 3;
    private static final int MSG_REQUEST_NETWORK_REQUEST = 4;
    private static final int MSG_RESPONSE_NETWORK_REQUEST = 5;

    private Gson gson = null;
    private IInputStreamService mService = null;
    //private Messenger mService = null; // Messenger for communicating with the service.
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
    public <T> T performRequest2(final Type type, Serializable request) {
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

        /*
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/

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


        final ParcelFileDescriptor output = performRequest22(request);
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


        /*

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        performRequest(type, request, os);

        if(type != null) {
            Reader targetReader = new InputStreamReader(new ByteArrayInputStream(os.toByteArray()));
            T result = gson.fromJson(targetReader, type);
            if(result != null) {
                Log.d(TAG, result.toString());
            }
            return result;
        } else {
            try {
                Log.d(TAG, "Test #1 read result: " + os.toByteArray().length + " str=" + os.toString("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;*/
    }

    public void performRequest(NextcloudRequest request, final ByteArrayOutputStream os) {
        try {
            request.accountName = getAccountName();

            Log.d(TAG, request.url);

            // send the input and output pfds
            //InputStream is = new ByteArrayInputStream("Colorless green ideas sleep furiously".getBytes("UTF-8"));


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

            ParcelFileDescriptor output = ParcelFileDescriptorUtil.pipeTo(os,
                    new IThreadListener() {

                        @Override
                        public void onThreadFinished(Thread thread) {
                            // service finished writing

                            Log.d(TAG, "Test #1 read result");
                            //Log.d(TAG, "Test #1 read result: " + os.toByteArray().length + " str=" + os.toString("UTF-8"));
                        }
                    });

            // blocks until result is ready
            mService.sendInputStreams(input, output);
            output.close(); // <-- this is required to halt the TransferThread

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Done!");
    }



    public ParcelFileDescriptor performRequest22(NextcloudRequest request) {
        try {
            request.accountName = getAccountName();

            Log.d(TAG, request.url);

            // send the input and output pfds
            //InputStream is = new ByteArrayInputStream("Colorless green ideas sleep furiously".getBytes("UTF-8"));


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
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
