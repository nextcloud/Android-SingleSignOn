package de.luhmer.owncloud.accountimporter.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;

import de.luhmer.owncloud.accountimporter.interfaces.IAccountsReceived;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by david on 28.05.14.
 */
public class AccountImporter {

    private static final String TAG = AccountImporter.class.getCanonicalName();
    private static final String PREF_FILE_NAME = "PrefNextcloudAccount";
    private static final String PREF_ACCOUNT_STRING = "PREF_ACCOUNT_STRING";

    public static boolean AddNewAccount() {
        //TODO
        return true;
    }

    public static void RequestAccounts(Context context, IAccountsReceived accountsReceivedCallback) {
        List<Account> accounts = FindAccounts(context);
        accountsReceivedCallback.accountsReceived(accounts);
    }

    public static boolean AccountsToImportAvailable(Context context) {
        return FindAccounts(context).size() > 0;
    }

    //TODO add multi account support
    public static Account GetCurrentAccount(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        String accountName = preferences.getString(PREF_ACCOUNT_STRING, "");
        return GetAccountForName(context, accountName);
    }

    //TODO add multi account support
    public static SingleAccount GetCurrentSingleAccount(Context context) throws AuthenticatorException, OperationCanceledException, IOException {
        return GetAuthToken(context, GetCurrentAccount(context));
    }

    //TODO add multi account support
    public static void SetCurrentAccount(Context context, Account account) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        preferences.edit().putString(PREF_ACCOUNT_STRING, account.name).commit();
    }




    // Find all currently installed nextcloud accounts on the phone
    private static List<Account> FindAccounts(Context context) {
        final AccountManager accMgr = AccountManager.get(context);
        final Account[] accounts = accMgr.getAccounts();

        List<Account> accountsAvailable = new ArrayList<>();
        for (Account account : accounts) {
            if (account.type.equals("nextcloud")) {
                accountsAvailable.add(account);
            }
        }
        return accountsAvailable;
    }

    private static final String AUTH_TOKEN = "NextcloudSSO";


    private static Account GetAccountForName(Context context, String name) {
        for (Account account : FindAccounts(context)) {
            if (account.name.equals(name)) {
                return account;
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static SingleAccount BlockingGetAuthToken(final Context context, final Account account) throws Exception {
        SingleAccount result = AsyncTaskHelper.ExecuteBlockingRequest(new Callable<SingleAccount>() {
            @Override
            public SingleAccount call() throws Exception {
                return AccountImporter.GetAuthToken(context, account);
            }
        });
        return result;
    }

    // Get the AuthToken (Password) for a selected account
    public static SingleAccount GetAuthToken(Context context, Account account) throws AuthenticatorException, OperationCanceledException, IOException {
        final AccountManager accMgr = AccountManager.get(context);
        Bundle options = new Bundle();
        accMgr.invalidateAuthToken(account.type, AUTH_TOKEN);
        //accMgr.getAuthToken(account, AUTH_TOKEN, null, true, new AccountManagerCallback<Bundle>() {


        // Synchronously access auth token
        Bundle future;
        if (context instanceof Activity) {
            future = accMgr.getAuthToken(account, AUTH_TOKEN, options, (Activity) context, null, null).getResult(); // Show activity
        } else {
            future = accMgr.getAuthToken(account, AUTH_TOKEN, options, true, null, null).getResult(); // Show notification instead
        }

        String auth_token = future.getString(AccountManager.KEY_AUTHTOKEN);
        String auth_account_type = future.getString(AccountManager.KEY_ACCOUNT_TYPE);
        accMgr.invalidateAuthToken(auth_account_type, auth_token);

        //String accountName = future.getString(AccountManager.KEY_ACCOUNT_NAME);
        String username = future.getString("username");
        String password = future.getString("password");
        String server_url = future.getString("server_url");
        boolean dhnv = future.getBoolean("disable_hostname_verification");

        return new SingleAccount(username, password, server_url, dhnv);
    }




















    private static final int MSG_CREATE_NEW_ACCOUNT = 3;

    private static final int MSG_REQUEST_NETWORK_REQUEST = 4;
    private static final int MSG_RESPONSE_NETWORK_REQUEST = 5;

    private Messenger mService = null; // Messenger for communicating with the service.
    private boolean mBound = false; // Flag indicating whether we have called bind on the service
    private Context context;


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


            final boolean stream = false;


            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, MSG_REQUEST_NETWORK_REQUEST, 0, 0);
            Bundle b = new Bundle();
            b.putString("account", "david@nextcloud.luhmbox.com");  // e.g. david@nextcloud.test.de
            b.putString("token", "test");    // token that the other app received by calling the AccountManager
            b.putString("endpoint", "/index.php/apps/news/api/v1-2/feeds"); // something like "/ocs/v1.php/apps/..."
            b.putString("method", "GET");   // Options: GET / POST / PUT / DELETE
            b.putString("header", "");
            b.putString("body", null);
            b.putBoolean("stream", stream);  // Do you want to stream the data?
            msg.setData(b);

            msg.replyTo = new Messenger(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == MSG_RESPONSE_NETWORK_REQUEST) {
                        if(stream) {

                        } else {
                            byte[] result = msg.getData().getByteArray("result");
                            try {
                                String resString = new String(result, "UTF-8");
                                Log.d(TAG, resString);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }
            });

            if(stream) {
                b.putInt("port", acceptStream());
            }

            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };


    private int acceptStream() {
        try {
            final ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket s = serverSocket.accept();
                        InputStream in = s.getInputStream();

                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = r.readLine()) != null) {
                            //total.append(line).append('\n');
                            Log.d(TAG, line);
                        }



                        /*
                        while(in.read() != -1) {
                            Log.d(TAG, "Received byte!");
                            // ignore result for now...
                        }*/

                        serverSocket.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();

            return port;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // TODO handle -1
    }


    public void onStart(Context context) {
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


    public void onStop() {
        // Unbind from the service
        if (mBound) {
            context.unbindService(mConnection);
            mBound = false;
        }

        this.context = null;
    }

}
