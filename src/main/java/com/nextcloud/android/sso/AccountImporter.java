/*
 * Nextcloud SingleSignOn
 *
 * @author David Luhmer
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

package com.nextcloud.android.sso;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.SSOException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.nextcloud.android.sso.Constants.NEXTCLOUD_FILES_ACCOUNT;
import static com.nextcloud.android.sso.Constants.NEXTCLOUD_SSO;
import static com.nextcloud.android.sso.Constants.NEXTCLOUD_SSO_EXCEPTION;
import static com.nextcloud.android.sso.Constants.SSO_SHARED_PREFERENCE;

public class AccountImporter {

    private static final String TAG = AccountImporter.class.getCanonicalName();
    private static final String PREF_ACCOUNT_STRING = "PREF_ACCOUNT_STRING";

    public static final int CHOOSE_ACCOUNT_SSO = 4242;
    public static final int REQUEST_AUTH_TOKEN_SSO = 4243;

    public static boolean AccountsToImportAvailable(Context context) {
        return findAccounts(context).size() > 0;
    }


    public static void pickNewAccount(Fragment fragment) throws NextcloudFilesAppNotInstalledException {
        if (appInstalledOrNot(fragment.getContext(), "com.nextcloud.client")) {

            // Clear all tokens first to prevent some caching issues..
            clearAllAuthTokens(fragment.getContext());

            Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{"nextcloud"},
                    true, null, null, null, null);
            fragment.startActivityForResult(intent, CHOOSE_ACCOUNT_SSO);
        } else {
            throw new NextcloudFilesAppNotInstalledException();
        }
    }

    private static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.v(TAG, e.getMessage());
        }
        return false;
    }

    // Find all currently installed nextcloud accounts on the phone
    public static List<Account> findAccounts(final Context context) {
        final AccountManager accMgr = AccountManager.get(context);
        final Account[] accounts = accMgr.getAccounts();

        List<Account> accountsAvailable = new ArrayList<>();
        for (final Account account : accounts) {
            if (account.type.equals("nextcloud")) {
                accountsAvailable.add(account);
            }
        }
        return accountsAvailable;
    }


    public static Account getAccountForName(Context context, String name) {
        for (Account account : findAccounts(context)) {
            if (account.name.equals(name)) {
                return account;
            }
        }
        return new Account(name,"nextcloud");
    }

    public static void clearAllAuthTokens(Context context) {
        SharedPreferences mPrefs = getSharedPreferences(context);
        for (String key : mPrefs.getAll().keySet()) {
            if (key.startsWith(PREF_ACCOUNT_STRING)) {
                mPrefs.edit().remove(key).apply();
            }
        }
    }

    public static SingleSignOnAccount getSingleSignOnAccount(Context context, final String accountName)
            throws NextcloudFilesAppAccountNotFoundException {
        SharedPreferences mPrefs = getSharedPreferences(context);
        String prefKey = getPrefKeyForAccount(accountName);

        if (mPrefs.contains(prefKey)) {
            try {
                return SingleSignOnAccount.fromString(mPrefs.getString(prefKey, null));
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "This should never happen!");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new NextcloudFilesAppAccountNotFoundException();
    }

    public static SingleSignOnAccount extractSingleSignOnAccountFromResponse(Intent intent, Context context) {
        Bundle future = intent.getBundleExtra(NEXTCLOUD_SSO);

        String accountName = future.getString(AccountManager.KEY_ACCOUNT_NAME);
        String username = future.getString(Constants.SSO_USERNAME);
        String token = future.getString(Constants.SSO_TOKEN);
        String server_url = future.getString(Constants.SSO_SERVER_URL);

        SharedPreferences mPrefs = getSharedPreferences(context);
        String prefKey = getPrefKeyForAccount(accountName);
        SingleSignOnAccount ssoAccount = new SingleSignOnAccount(accountName, username, token, server_url);
        try {
            mPrefs.edit().putString(prefKey, SingleSignOnAccount.toString(ssoAccount)).apply();
        } catch (IOException e) {
            Log.e(TAG, "SSO failed", e);
        }
        return ssoAccount;
    }


    public interface IAccountAccessGranted {
        void accountAccessGranted(SingleSignOnAccount singleSignOnAccount);
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data, Activity activity,
                                        IAccountAccessGranted callback) {
        onActivityResult(requestCode, resultCode, data, activity, null, callback);
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data, Fragment fragment,
                                        IAccountAccessGranted callback) {
        onActivityResult(requestCode, resultCode, data, null, fragment, callback);
    }

    private static void onActivityResult(int requestCode, int resultCode, Intent data, Activity activity,
                                         Fragment fragment, IAccountAccessGranted callback) {
        Context context = (activity != null) ? activity : fragment.getContext();

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_ACCOUNT_SSO:
                    try {
                        if (activity != null) {
                            requestAuthToken(activity, data);
                        } else {
                            requestAuthToken(fragment, data);
                        }
                    } catch (NextcloudFilesAppNotSupportedException e) {
                        UiExceptionManager.showDialogForException(context, e);
                    }
                    break;
                case REQUEST_AUTH_TOKEN_SSO:
                    SingleSignOnAccount singleSignOnAccount = extractSingleSignOnAccountFromResponse(data, context);
                    callback.accountAccessGranted(singleSignOnAccount);
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case CHOOSE_ACCOUNT_SSO:
                    Toast.makeText(context, R.string.select_account_unknown_error_toast, Toast.LENGTH_LONG).show();
                    break;
                case REQUEST_AUTH_TOKEN_SSO:
                    try {
                        handleFailedAuthRequest(data);
                    } catch (SSOException e) {
                        UiExceptionManager.showDialogForException(context, e);
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        //e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static void handleFailedAuthRequest(Intent data) throws SSOException {
        String exception = data.getStringExtra(NEXTCLOUD_SSO_EXCEPTION);
        throw SSOException.parseNextcloudCustomException(new Exception(exception));
    }

    public static void requestAuthToken(Fragment fragment, Intent intent) throws NextcloudFilesAppNotSupportedException {
        Intent authIntent = buildRequestAuthTokenIntent(fragment.getContext(), intent);
        try {
            fragment.startActivityForResult(authIntent, REQUEST_AUTH_TOKEN_SSO);
        } catch (ActivityNotFoundException e) {
            throw new NextcloudFilesAppNotSupportedException();
        }
    }

    public static void requestAuthToken(Activity activity, Intent intent) throws NextcloudFilesAppNotSupportedException {
        Intent authIntent = buildRequestAuthTokenIntent(activity, intent);
        try {
            activity.startActivityForResult(authIntent, REQUEST_AUTH_TOKEN_SSO);
        } catch (ActivityNotFoundException e) {
            throw new NextcloudFilesAppNotSupportedException();
        }
    }

    private static Intent buildRequestAuthTokenIntent(Context context, Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        Account account = AccountImporter.getAccountForName(context, accountName);
        Intent authIntent = new Intent();
        authIntent.setComponent(new ComponentName("com.nextcloud.client",
                "com.owncloud.android.ui.activity.SsoGrantPermissionActivity"));
        authIntent.putExtra(NEXTCLOUD_FILES_ACCOUNT, account);
        return authIntent;
    }


    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SSO_SHARED_PREFERENCE, Context.MODE_PRIVATE);
    }

    protected static String getPrefKeyForAccount(String accountName) {
        return PREF_ACCOUNT_STRING + accountName;
    }
}
