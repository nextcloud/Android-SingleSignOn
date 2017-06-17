package de.luhmer.owncloud.accountimporter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import de.luhmer.owncloud.accountimporter.helper.AccountImporter;
import de.luhmer.owncloud.accountimporter.helper.SingleAccount;
import de.luhmer.owncloud.accountimporter.interfaces.IAccountImport;

public class ImportAccountsDialogActivity extends AppCompatActivity implements IAccountImport {

    private static final String TAG = ImportAccountsDialogActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_accounts_dialog);

        ImportAccountsDialogFragment.show(this, this);
    }



    @Override
    public void accountAccessGranted(Account account) {
        Log.d(TAG, "accountAccessGranted() called with: account = [" + account + "]");


        try {
            AccountImporter.GetCurrentAccount(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}