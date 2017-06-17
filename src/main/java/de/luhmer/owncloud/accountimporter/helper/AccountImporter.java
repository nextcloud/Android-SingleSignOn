package de.luhmer.owncloud.accountimporter.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        Object result = new AsyncTaskGetAuthToken(context, account).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        if(result instanceof Exception) {
            throw((Exception) result);
        } else if(result instanceof SingleAccount){
            return (SingleAccount) result;
        }
        throw new IllegalStateException("IllegalState!");
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


    private static class AsyncTaskGetAuthToken extends AsyncTask<Void, Void, Object> {

        private Context context;
        private Account account;

        AsyncTaskGetAuthToken(Context context, Account account) {
            this.context = context;
            this.account = account;
        }

        @Override
        protected Object doInBackground(Void... params) {
            try {
                return AccountImporter.GetAuthToken(context, account);
            } catch (Exception e) {
                return e;
            }
        }
    }

}
