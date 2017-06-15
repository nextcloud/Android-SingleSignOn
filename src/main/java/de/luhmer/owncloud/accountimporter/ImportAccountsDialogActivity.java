package de.luhmer.owncloud.accountimporter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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
    public void accountAccessGranted(SingleAccount account) {
        Log.d(TAG, "accountAccessGranted() called with: account = [" + account + "]");
    }
}
