package de.luhmer.owncloud.accountimporter.helper;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import de.luhmer.owncloud.accountimporter.AccountImporter;
import de.luhmer.owncloud.accountimporter.exceptions.CurrentAccountNotFoundException;
import de.luhmer.owncloud.accountimporter.exceptions.NoCurrentAccountSelectedException;
import de.luhmer.owncloud.accountimporter.model.SingleSignOnAccount;

import static android.content.Context.MODE_PRIVATE;

public class SingleAccountHelper {

    private static final String TAG = SingleAccountHelper.class.getCanonicalName();
    private static final String PREF_FILE_NAME = "PrefNextcloudAccount";
    private static final String PREF_ACCOUNT_STRING = "PREF_ACCOUNT_STRING";

    public static Account GetCurrentAccount(Context context) throws NoCurrentAccountSelectedException, CurrentAccountNotFoundException {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        String accountName = preferences.getString(PREF_ACCOUNT_STRING, null);
        if(accountName == null) {
            throw new NoCurrentAccountSelectedException();
        }
        Account account = AccountImporter.GetAccountForName(context, accountName);
        if(account == null) {
            throw new CurrentAccountNotFoundException();
        }
        return account;
    }

    public static SingleSignOnAccount GetCurrentSingleAccount(Context context) throws AuthenticatorException, OperationCanceledException, IOException, NoCurrentAccountSelectedException, CurrentAccountNotFoundException {
        return AccountImporter.GetAuthToken(context, GetCurrentAccount(context));
    }

    public static void SetCurrentAccount(Context context, Account account) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        preferences.edit().putString(PREF_ACCOUNT_STRING, account.name).commit();
    }

}
