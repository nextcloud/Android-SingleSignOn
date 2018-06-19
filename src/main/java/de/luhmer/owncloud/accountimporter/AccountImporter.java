package de.luhmer.owncloud.accountimporter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import de.luhmer.owncloud.accountimporter.exceptions.NextcloudFilesAppNotInstalledException;
import de.luhmer.owncloud.accountimporter.helper.AsyncTaskHelper;
import de.luhmer.owncloud.accountimporter.model.SingleSignOnAccount;

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

    private static final String AUTH_TOKEN = "NextcloudSSO";

    public static final int CHOOSE_ACCOUNT_SSO = 4242;

    public static boolean AccountsToImportAvailable(Context context) {
        return FindAccounts(context).size() > 0;
    }


    public static void PickNewAccount(android.support.v4.app.Fragment fragment) throws NextcloudFilesAppNotInstalledException {
        if(AppInstalledOrNot(fragment.getContext(), "com.nextcloud.client")) {
            Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{"nextcloud"},
                    true, null, null, null, null);
            fragment.startActivityForResult(intent, CHOOSE_ACCOUNT_SSO);
        } else {
            throw new NextcloudFilesAppNotInstalledException();
        }
    }

    private static boolean AppInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
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
    public static SingleSignOnAccount GetAuthToken(Context context, Account account) throws AuthenticatorException, OperationCanceledException, IOException {
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
        String token = future.getString("token");
        String server_url = future.getString("server_url");
        boolean dhnv = future.getBoolean("disable_hostname_verification");

        return new SingleSignOnAccount(account.name, username, token, server_url, dhnv);
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
