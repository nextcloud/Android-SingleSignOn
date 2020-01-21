package com.nextcloud.android.sso.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.nextcloud.android.sso.Constants;
import com.nextcloud.android.sso.aidl.IInputStreamService;
import com.nextcloud.android.sso.aidl.IThreadListener;
import com.nextcloud.android.sso.aidl.NextcloudRequest;
import com.nextcloud.android.sso.aidl.ParcelFileDescriptorUtil;
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;

import static com.nextcloud.android.sso.exceptions.SSOException.parseNextcloudCustomException;

public class AidlNetworkRequest extends NetworkRequest {
    private static final String TAG = AidlNetworkRequest.class.getCanonicalName();

    private IInputStreamService mService = null;
    private final AtomicBoolean mBound = new AtomicBoolean(false); // Flag indicating whether we have called bind on the service

    AidlNetworkRequest(@NonNull Context context, @NonNull SingleSignOnAccount account, @NonNull NextcloudAPI.ApiConnectedListener callback) {
        super(context, account, callback);
    }


    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.v(TAG, "onServiceConnected [" + Thread.currentThread().getName() + "]");

            mService = IInputStreamService.Stub.asInterface(service);
            mBound.set(true);
            synchronized (mBound) {
                mBound.notifyAll();
            }
            mCallback.onConnected();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "ServiceDisconnected [" +className.toString() + "]");
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound.set(false);

            if (!mDestroyed) {
                Log.d(TAG, "Reconnecting lost service connection");
                connectApiWithBackoff();
            }
        }
    };

    public void connect(String type) {
        if (mDestroyed) {
            throw new IllegalStateException("API already stopped");
        }

        super.connect(type);

        String componentName = Constants.PACKAGE_NAME_PROD;
        if (type.equals(Constants.ACCOUNT_TYPE_DEV)) {
            componentName = Constants.PACKAGE_NAME_DEV;
        }

        try {
            Intent intentService = new Intent();
            intentService.setComponent(new ComponentName(componentName,
                                                         "com.owncloud.android.services.AccountManagerService"));
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
        super.stop();

        // Unbind from the service
        if (mBound.get()) {
            if (mContext != null) {
                mContext.unbindService(mConnection);
            } else {
                Log.e(TAG, "Context was null, cannot unbind nextcloud single sign-on service connection!");
            }
            mBound.set(false);
            mContext = null;
        }
    }

    public void reconnect() {
        if (mContext != null) {
            mContext.unbindService(mConnection);
        } else {
            Log.e(TAG, "Context was null, cannot unbind nextcloud single sign-on service connection!");
        }
        // the callback "onServiceDisconnected" will trigger a reconnect automatically. No need to do anything here..
    }

    private void waitForApi() throws NextcloudApiNotRespondingException {
        synchronized (mBound) {
            // If service is not bound yet.. wait
            if(!mBound.get()) {
                Log.v(TAG, "[waitForApi] - api not ready yet.. waiting [" + Thread.currentThread().getName() +  "]");
                try {
                    mBound.wait(10000); // wait up to 10 seconds

                    // If api is still not bound after 10 seconds.. try reconnecting
                    if(!mBound.get()) {
                        /*
                        // try one reconnect
                        reconnect();

                        mBound.wait(10000); // wait up to 10 seconds
                        // If api is still not bound after 10 seconds.. throw an exception
                        if(!mBound.get()) {
                            throw new NextcloudApiNotRespondingException();
                        }
                        */
                        throw new NextcloudApiNotRespondingException();
                    }
                } catch (InterruptedException ex) {
                    Log.e(TAG, "WaitForAPI failed", ex);
                }
            }
        }
    }

    /**
     * The InputStreams needs to be closed after reading from it
     *
     * @param request {@link NextcloudRequest} request to be executed on server via Files app
     * @param requestBodyInputStream inputstream to be sent to the server
     * @return InputStream answer from server as InputStream
     * @throws Exception or SSOException
     */
    public Response performNetworkRequestV2(NextcloudRequest request, InputStream requestBodyInputStream) throws Exception {
        ParcelFileDescriptor output = performAidlNetworkRequestV2(request, requestBodyInputStream);
        InputStream os = new ParcelFileDescriptor.AutoCloseInputStream(output);
        ExceptionResponse response = deserializeObjectV2(os);

        // Handle Remote Exceptions
        if (response.getException() != null) {
            if (response.getException().getMessage() != null) {
                throw parseNextcloudCustomException(response.getException());
            }
            throw response.getException();
        }
        return new Response(os, response.headers);
    }

    /**
     * The InputStreams needs to be closed after reading from it
     *
     * @param request                {@link NextcloudRequest} request to be executed on server via Files app
     * @param requestBodyInputStream inputstream to be sent to the server
     * @return InputStream answer from server as InputStream
     * @throws Exception or SSOException
     */
    public InputStream performNetworkRequest(NextcloudRequest request, InputStream requestBodyInputStream) throws Exception {
        InputStream os = null;
        Exception exception;
        try {
            ParcelFileDescriptor output = performAidlNetworkRequest(request, requestBodyInputStream);
            os = new ParcelFileDescriptor.AutoCloseInputStream(output);
            exception = deserializeObject(os);
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            exception = e;
        }

        // Handle Remote Exceptions
        if (exception != null) {
            if (exception.getMessage() != null) {
                exception = parseNextcloudCustomException(exception);
            }
            throw exception;
        }
        return os;
    }

    /**
     * DO NOT CALL THIS METHOD DIRECTLY - use @link(performNetworkRequest) instead
     *
     * @param request
     * @return
     * @throws IOException
     */
    private ParcelFileDescriptor performAidlNetworkRequest(NextcloudRequest request, InputStream requestBodyInputStream)
            throws IOException, RemoteException, NextcloudApiNotRespondingException {

        // Check if we are on the main thread
        if(Looper.myLooper() == Looper.getMainLooper()) {
            throw new NetworkOnMainThreadException();
        }

        if(mDestroyed) {
            throw new IllegalStateException("Nextcloud API already destroyed. Please report this issue.");
        }

        // Wait for api to be initialized
        waitForApi();

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

        ParcelFileDescriptor requestBodyParcelFileDescriptor = null;
        if(requestBodyInputStream != null) {
            requestBodyParcelFileDescriptor = ParcelFileDescriptorUtil.pipeFrom(requestBodyInputStream,
                    new IThreadListener() {
                        @Override
                        public void onThreadFinished(Thread thread) {
                            Log.d(TAG, "copy data from service finished");
                        }
                    });
        }

        ParcelFileDescriptor output;
        if(requestBodyParcelFileDescriptor != null) {
            output = mService.performNextcloudRequestAndBodyStream(input, requestBodyParcelFileDescriptor);
        } else {
            output = mService.performNextcloudRequest(input);
        }

        return output;
    }

    /**
     * DO NOT CALL THIS METHOD DIRECTLY - use @link(performNetworkRequestV2) instead
     *
     * @param request
     * @return
     * @throws IOException
     */
    private ParcelFileDescriptor performAidlNetworkRequestV2(NextcloudRequest request,
                                                             InputStream requestBodyInputStream)
            throws IOException, RemoteException, NextcloudApiNotRespondingException {

        // Check if we are on the main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new NetworkOnMainThreadException();
        }

        // Wait for api to be initialized
        waitForApi();

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

        ParcelFileDescriptor requestBodyParcelFileDescriptor = null;
        if (requestBodyInputStream != null) {
            requestBodyParcelFileDescriptor = ParcelFileDescriptorUtil.pipeFrom(requestBodyInputStream,
                    new IThreadListener() {
                        @Override
                        public void onThreadFinished(Thread thread) {
                            Log.d(TAG, "copy data from service finished");
                        }
                    });
        }

        ParcelFileDescriptor output;
        if (requestBodyParcelFileDescriptor != null) {
            output = mService.performNextcloudRequestAndBodyStreamV2(input, requestBodyParcelFileDescriptor);
        } else {
            output = mService.performNextcloudRequestV2(input);
        }

        return output;
    }

    private static <T> T deserializeObject(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        T result = (T) ois.readObject();
        return result;
    }

    private ExceptionResponse deserializeObjectV2(InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        ArrayList<PlainHeader> headerList = new ArrayList<>();
        Exception exception = (Exception) ois.readObject();

        if (exception == null) {
            String headers = (String) ois.readObject();
            ArrayList list = new Gson().fromJson(headers, ArrayList.class);

            for (Object o : list) {
                LinkedTreeMap treeMap = (LinkedTreeMap) o;
                headerList.add(new PlainHeader((String) treeMap.get("name"), (String) treeMap.get("value")));
            }
        }

        return new ExceptionResponse(exception, headerList);
    }

    public class PlainHeader implements Serializable {
        private String name;
        private String value;

        PlainHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        private void writeObject(ObjectOutputStream oos) throws IOException{
            oos.writeObject(name);
            oos.writeObject(value);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            name = (String) in.readObject();
            value = (String) in.readObject();
        }
    }

    private class ExceptionResponse {
        private Exception exception;
        private ArrayList<PlainHeader> headers;

        public ExceptionResponse(Exception exception, ArrayList<PlainHeader> headers) {
            this.exception = exception;
            this.headers = headers;
        }

        public Exception getException() {
            return exception;
        }
    }
}
