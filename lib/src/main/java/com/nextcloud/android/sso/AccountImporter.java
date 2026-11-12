/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021-2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018-2019 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-FileCopyrightText: 2017-2019 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.nextcloud.android.sso.Constants.INTERNAL_NEXTCLOUD_FILES_ACCOUNT;
import static com.nextcloud.android.sso.Constants.INTERNAL_NEXTCLOUD_SSO;
import static com.nextcloud.android.sso.Constants.INTERNAL_NEXTCLOUD_SSO_EXCEPTION;
import static com.nextcloud.android.sso.Constants.INTERNAL_SSO_SHARED_PREFERENCE;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountPermissionNotGrantedException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotSupportedException;
import com.nextcloud.android.sso.exceptions.SSOException;
import com.nextcloud.android.sso.exceptions.UnknownErrorException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;
import com.nextcloud.android.sso.ui.UiExceptionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.reactivex.annotations.Nullable;

public class AccountImporter {

    private static final String TAG = AccountImporter.class.getCanonicalName();
    private static final String PREF_ACCOUNT_STRING = "PREF_ACCOUNT_STRING";
    private static final String AUTH_TOKEN_SSO = "SSO";

    public static final int CHOOSE_ACCOUNT_SSO = 4242;
    public static final int REQUEST_AUTH_TOKEN_SSO = 4243;
    public static final int REQUEST_GET_ACCOUNTS_PERMISSION = 4244;

    private static SharedPreferences SHARED_PREFERENCES;

    public static boolean accountsToImportAvailable(Context context) {
        return !findAccounts(context).isEmpty();
    }

    /**
     * Prompt dialog to select existing or create a new account
     * As soon as an account has been imported, we will continue in #onActivityResult()
     * <p>
     * In real live applications, you won't import an account on each app start, but remember the imported account via SingleAccountHelper.
     *
     * @see <a href="https://developer.android.com/training/basics/intents/result"><code>ActivityResultContract</code></a>
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void pickNewAccount(Activity activity) throws NextcloudFilesAppNotInstalledException,
            AndroidGetAccountsPermissionNotGranted {
        checkAndroidAccountPermissions(activity);

        if (appInstalledOrNot(activity)) {
            Intent intent = AccountManager.newChooseAccountIntent(null, null, FilesAppTypeRegistry.getInstance().getAccountTypes(),
                    true, null, AUTH_TOKEN_SSO, null, null);
            activity.startActivityForResult(intent, CHOOSE_ACCOUNT_SSO);
        } else {
            throw new NextcloudFilesAppNotInstalledException(activity);
        }
    }

    /**
     * @see <a href="https://developer.android.com/training/basics/intents/result"><code>ActivityResultContract</code></a>
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void requestAndroidAccountPermissionsAndPickAccount(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.GET_ACCOUNTS},
            REQUEST_GET_ACCOUNTS_PERMISSION);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void checkAndroidAccountPermissions(Context context) throws AndroidGetAccountsPermissionNotGranted {
        // https://developer.android.com/reference/android/accounts/AccountManager#getAccountsByType(java.lang.String)
        // Caller targeting API level below Build.VERSION_CODES.O that have not been granted the 
        // Manifest.permission.GET_ACCOUNTS permission, will only see those accounts managed by 
        // AbstractAccountAuthenticators whose signature matches the client.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Do something for lollipop and above versions
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permission not granted yet!");
                throw new AndroidGetAccountsPermissionNotGranted(context);
            } else {
                Log.d(TAG, "Permission granted!");
            }
        }
    }

    private static boolean appInstalledOrNot(Context context) {
        boolean returnValue = false;
        PackageManager pm = context.getPackageManager();
        for (final var appType : FilesAppTypeRegistry.getInstance().getTypes()) {
            try {
                pm.getPackageInfo(appType.packageId(), PackageManager.GET_ACTIVITIES);
                returnValue = true;
                break;
            } catch (PackageManager.NameNotFoundException e) {
                Log.v(TAG, Optional.ofNullable(e.getMessage()).orElse(e.toString()));
            }
        }
        return returnValue;
    }

    /**
     * Find all currently installed nextcloud accounts on the phone
     */
    public static List<Account> findAccounts(final Context context) {
        final AccountManager accMgr = AccountManager.get(context);
        final Account[] accounts = accMgr.getAccounts();

        List<Account> accountsAvailable = new ArrayList<>();
        for (final Account account : accounts) {
            for (String accountType : FilesAppTypeRegistry.getInstance().getAccountTypes()) {
                if (accountType.equals(account.type)) {
                    accountsAvailable.add(account);
                }
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
        return null;
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
            } catch (ClassNotFoundException | IOException e) {
                Log.e(TAG, "[getSingleSignOnAccount]", e);
            }
        }
        throw new NextcloudFilesAppAccountNotFoundException(context, accountName);
    }

    private static SingleSignOnAccount extractSingleSignOnAccountFromResponse(Intent intent, Context context) {
        Bundle future = intent.getBundleExtra(INTERNAL_NEXTCLOUD_SSO);

        String accountName = future.getString(AccountManager.KEY_ACCOUNT_NAME);
        String userId = future.getString(Constants.INTERNAL_SSO_USER_ID);
        if (userId == null) {
            // backwards compatibility
            userId = future.getString("username");
        }
        String token = future.getString(Constants.INTERNAL_SSO_TOKEN);
        String serverUrl = future.getString(Constants.INTERNAL_SSO_SERVER_URL);
        String type = future.getString("accountType");

        SharedPreferences mPrefs = getSharedPreferences(context);
        String prefKey = getPrefKeyForAccount(accountName);
        SingleSignOnAccount ssoAccount = new SingleSignOnAccount(accountName, userId, token, serverUrl, type);
        try {
            mPrefs.edit().putString(prefKey, SingleSignOnAccount.toString(ssoAccount)).apply();
        } catch (IOException e) {
            Log.e(TAG, "SSO failed", e);
        }
        return ssoAccount;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public interface IAccountAccessGranted {
        void accountAccessGranted(SingleSignOnAccount singleSignOnAccount);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void onActivityResult(int requestCode, int resultCode, Intent data, Activity activity, IAccountAccessGranted successCallback, Consumer<Throwable> failureCallback)
            throws AccountImportCancelledException {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_ACCOUNT_SSO:
                    try {
                        requestAuthToken(activity, data);
                    } catch (NextcloudFilesAppNotSupportedException |
                             NextcloudFilesAppAccountPermissionNotGrantedException e) {
                        UiExceptionManager.showDialogForException(activity, e, failureCallback::accept);
                    }
                    break;
                case REQUEST_AUTH_TOKEN_SSO:
                    SingleSignOnAccount singleSignOnAccount = extractSingleSignOnAccountFromResponse(data, activity);
                    successCallback.accountAccessGranted(singleSignOnAccount);
                    break;
                case REQUEST_GET_ACCOUNTS_PERMISSION:
                    try {
                        pickNewAccount(activity);
                    } catch (NextcloudFilesAppNotInstalledException |
                             AndroidGetAccountsPermissionNotGranted e) {
                        UiExceptionManager.showDialogForException(activity, e, failureCallback::accept);
                    }
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case CHOOSE_ACCOUNT_SSO:
                    // nothing to do here
                    throw new AccountImportCancelledException(activity);
                case REQUEST_AUTH_TOKEN_SSO:
                    try {
                        handleFailedAuthRequest(activity, data);
                    } catch (SSOException e) {
                        UiExceptionManager.showDialogForException(activity, e, failureCallback::accept);
                    } catch (Exception e) {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, e.getMessage(), e);
                        failureCallback.accept(e);
                    }
                    break;
                case REQUEST_GET_ACCOUNTS_PERMISSION:
                    UiExceptionManager.showDialogForException(activity, new AndroidGetAccountsPermissionNotGranted(activity), failureCallback::accept);
                    break;
                default:
                    break;
            }
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, Activity activity, Fragment fragment, Consumer<Throwable> failureCallback) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_GET_ACCOUNTS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissions have been granted… start pick account dialog
                    try {
                        pickNewAccount(activity);
                    } catch (NextcloudFilesAppNotInstalledException |
                             AndroidGetAccountsPermissionNotGranted e) {
                        UiExceptionManager.showDialogForException(activity, e, failureCallback::accept);
                    }
                } else {
                    // user declined the permission request…
                    UiExceptionManager.showDialogForException(activity, new AndroidGetAccountsPermissionNotGranted(activity), failureCallback::accept);
                }
                break;
            default:
                break;
        }

    }

    private static void handleFailedAuthRequest(@NonNull Context context, @Nullable Intent data) throws SSOException {
        if (data != null) {
            String exception = data.getStringExtra(INTERNAL_NEXTCLOUD_SSO_EXCEPTION);
            throw SSOException.parseNextcloudCustomException(context, new Exception(exception));
        } else {
            Log.e(TAG, "handleFailedAuthRequest failed - data is null");
            throw new UnknownErrorException("Authentication request failed - no details available");
        }
    }

    public static void authenticateSingleSignAccount(Fragment fragment, SingleSignOnAccount account) throws NextcloudFilesAppNotSupportedException, NextcloudFilesAppAccountPermissionNotGrantedException {
        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        requestAuthToken(fragment, intent);
    }

    public static void authenticateSingleSignAccount(Activity activity, SingleSignOnAccount account) throws NextcloudFilesAppNotSupportedException, NextcloudFilesAppAccountPermissionNotGrantedException {
        Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
        requestAuthToken(activity, intent);
    }

    public static void requestAuthToken(Fragment fragment, Intent intent) throws NextcloudFilesAppNotSupportedException, NextcloudFilesAppAccountPermissionNotGrantedException {
        Intent authIntent = buildRequestAuthTokenIntent(fragment.getContext(), intent);
        try {
            fragment.startActivityForResult(authIntent, REQUEST_AUTH_TOKEN_SSO);
        } catch (ActivityNotFoundException e) {
            throw new NextcloudFilesAppNotSupportedException(fragment.requireContext());
        }
    }

    public static void requestAuthToken(Activity activity, Intent intent) throws NextcloudFilesAppNotSupportedException, NextcloudFilesAppAccountPermissionNotGrantedException {
        Intent authIntent = buildRequestAuthTokenIntent(activity, intent);
        try {
            activity.startActivityForResult(authIntent, REQUEST_AUTH_TOKEN_SSO);
        } catch (ActivityNotFoundException e) {
            throw new NextcloudFilesAppNotSupportedException(activity);
        }
    }

    private static Intent buildRequestAuthTokenIntent(Context context, Intent intent) throws NextcloudFilesAppAccountPermissionNotGrantedException {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        Account account = AccountImporter.getAccountForName(context, accountName);
        if (account == null) {
            throw new NextcloudFilesAppAccountPermissionNotGrantedException(context);
        }

        String componentName = FilesAppTypeRegistry.getInstance().findByAccountType(account.type).packageId();

        Intent authIntent = new Intent();
        authIntent.setComponent(new ComponentName(componentName,
                "com.owncloud.android.ui.activity.SsoGrantPermissionActivity"));
        authIntent.putExtra(INTERNAL_NEXTCLOUD_FILES_ACCOUNT, account);
        return authIntent;
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        if (SHARED_PREFERENCES != null) {
            return SHARED_PREFERENCES;
        } else {
            return context.getSharedPreferences(INTERNAL_SSO_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        }
    }

    protected static String getPrefKeyForAccount(String accountName) {
        return PREF_ACCOUNT_STRING + accountName;
    }


    /**
     * Allows developers to set the shared preferences that the account information should be stored in.
     * This is helpful when writing unit tests
     */
    public static void setSharedPreferences(SharedPreferences sharedPreferences) {
        AccountImporter.SHARED_PREFERENCES = sharedPreferences;
    }
}
