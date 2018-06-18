package com.nextcloud.android.sso.helper;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.exceptions.CurrentAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

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
