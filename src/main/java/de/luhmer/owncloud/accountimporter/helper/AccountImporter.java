package de.luhmer.owncloud.accountimporter.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.luhmer.owncloud.accountimporter.ImportAccountsDialogFragment;
import de.luhmer.owncloud.accountimporter.R;
import de.luhmer.owncloud.accountimporter.interfaces.IAccountImport;
import de.luhmer.owncloud.accountimporter.interfaces.IAccountsReceived;

import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.GET_PROVIDERS;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_SERVICES;

/**
 * Created by david on 28.05.14.
 */
public class AccountImporter {

    private static final String TAG = AccountImporter.class.getCanonicalName();

    private static final int MSG_REQUEST_PASSWORD = 1;
    private static final int MSG_RESPONSE_PASSWORD = 2;
    private static final int MSG_CREATE_NEW_ACCOUNT = 3;

    private Messenger mService = null; // Messenger for communicating with the service.
    private boolean mBound = false; // Flag indicating whether we have called bind on the service
    private Context context;
    private IAccountsReceived accountsReceivedCallback;

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

            requestAccounts();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    public boolean requestAccounts() {
        if (!mBound) return false;

        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, MSG_REQUEST_PASSWORD, 0, 0);
        msg.replyTo = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_RESPONSE_PASSWORD) {
                    ArrayList<HashMap<String, String>> accounts = (ArrayList<HashMap<String, String>>) msg.getData().getSerializable("accounts");
                    accountsReceivedCallback.accountsReceived(accounts);
                }
            }
        });

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean addNewAccount() {
        if (!mBound) return false;

        Message msg = Message.obtain(null, MSG_CREATE_NEW_ACCOUNT, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void onStart(Context context, IAccountsReceived accountsReceivedCallback) {
        this.context = context;
        this.accountsReceivedCallback = accountsReceivedCallback;

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


    public void onStop() {
        // Unbind from the service
        if (mBound) {
            context.unbindService(mConnection);
            mBound = false;
        }

        this.context = null;
        this.accountsReceivedCallback = null;
    }
}
