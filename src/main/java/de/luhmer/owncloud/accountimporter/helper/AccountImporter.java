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

    public static boolean AddNewAccount() {
        //TODO
        return true;
    }

    public static void RequestAccounts(Context context, IAccountsReceived accountsReceivedCallback) {
        List<Account> accounts = FindAccounts(context);
        accountsReceivedCallback.accountsReceived(accounts);
    }


    // Find all currently installed nextcloud accounts on the phone
    public static List<Account> FindAccounts(Context context) {
        final AccountManager accMgr = AccountManager.get(context);
        final Account[] accounts = accMgr.getAccounts();

        List<Account> accountsAvailable = new ArrayList<>();
        for (Account account : accounts) {
            if(account.type.equals("nextcloud")) {
                accountsAvailable.add(account);
            }
        }
        return accountsAvailable;
    }

    // Get the AuthToken (Password) for a selected account
    public static void GetAuthToken(Activity activity, Account account, final AccountApproved callback) {
        final AccountManager accMgr = AccountManager.get(activity);
        Bundle options = new Bundle();
        //accMgr.invalidateAuthToken(account.type, "NextcloudSSO"); // Invalidate credentials (//TODO is this really necessary after we're done debugging?)
        accMgr.getAuthToken(account, "NextcloudSSO", options, activity, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    String username = future.getResult().getString("username");
                    String password = future.getResult().getString("password");
                    String server   = future.getResult().getString("server");
                    callback.onAccessGranted(new SingleAccount(username, password, server));
                } catch (Exception ex) {
                    callback.onError(ex);
                }
            }
        }, null);
    }

    public interface AccountApproved {
        void onAccessGranted(SingleAccount singleAccount);
        void onError(Exception exception);
    }
}
