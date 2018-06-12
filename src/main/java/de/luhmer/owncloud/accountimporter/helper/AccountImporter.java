package de.luhmer.owncloud.accountimporter.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.content.Context.MODE_PRIVATE;

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

public class AccountImporter {

    private static final String TAG = AccountImporter.class.getCanonicalName();
    private static final String PREF_FILE_NAME = "PrefNextcloudAccount";
    private static final String PREF_ACCOUNT_STRING = "PREF_ACCOUNT_STRING";

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
    public static SingleSignOnAccount GetCurrentSingleAccount(Context context) throws AuthenticatorException, OperationCanceledException, IOException {
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


    public static Account GetAccountForName(Context context, String name) {
        for (Account account : FindAccounts(context)) {
            if (account.name.equals(name)) {
                return account;
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static SingleSignOnAccount BlockingGetAuthToken(final Context context, final Account account) throws Exception {
        SingleSignOnAccount result = AsyncTaskHelper.ExecuteBlockingRequest(new Callable<SingleSignOnAccount>() {
            @Override
            public SingleSignOnAccount call() throws Exception {
                return AccountImporter.GetAuthToken(context, account);
            }
        });
        return result;
    }

    // Get the AuthToken (Password) for a selected account
    public static SingleSignOnAccount GetAuthToken(Context context, Account account) throws AuthenticatorException, 
            OperationCanceledException, IOException {
        final AccountManager accMgr = AccountManager.get(context);
        Bundle options = new Bundle();
        accMgr.invalidateAuthToken(account.type, AUTH_TOKEN);

        // Synchronously access auth token
        Bundle future;
        if (context instanceof Activity) {
            // Show activity
            future = accMgr.getAuthToken(account, AUTH_TOKEN, options, (Activity) context, null, null).getResult(); 
        } else {
            // Show notification instead
            future = accMgr.getAuthToken(account, AUTH_TOKEN, options, true, null, null).getResult();
        }

        String auth_token = future.getString(AccountManager.KEY_AUTHTOKEN);
        String auth_account_type = future.getString(AccountManager.KEY_ACCOUNT_TYPE);
        accMgr.invalidateAuthToken(auth_account_type, auth_token);

        String username = future.getString("username");
        String token = future.getString("token");
        String server_url = future.getString("server_url");
        String packageName = context.getPackageName();

        return new SingleSignOnAccount(account.name, username, token, server_url, packageName);
    }


    public static SingleSignOnAccount GetAuthTokenInSeparateThread(final Context context, final Account account) {
        SingleSignOnAccount ssoAccount = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<SingleSignOnAccount> callable = new Callable<SingleSignOnAccount>() {
            @Override
            public SingleSignOnAccount call() throws AuthenticatorException, OperationCanceledException, IOException {
                return AccountImporter.GetAuthToken(context, account);

            }
        };
        Future<SingleSignOnAccount> future = executor.submit(callable);
        try {
            ssoAccount = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        return ssoAccount;
    }
}
