package de.luhmer.owncloud.accountimporter;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.luhmer.owncloud.accountimporter.adapter.AccountImporterAdapter;
import de.luhmer.owncloud.accountimporter.helper.AccountImporter;
import de.luhmer.owncloud.accountimporter.helper.SingleAccount;
import de.luhmer.owncloud.accountimporter.interfaces.IAccountImport;
import de.luhmer.owncloud.accountimporter.interfaces.IAccountsReceived;


/**
 * Created  by David on 16.05.2014.
 * Modified by David on 15.06.2017.
 */
@SuppressLint("ValidFragment")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ImportAccountsDialogFragment extends DialogFragment {

    private ListView lvAccounts;
    private ProgressBar progressBar;

    public IAccountImport accountImportCallback;
    private AccountImporter accountImporter;
    private static final String TAG = ImportAccountsDialogFragment.class.getCanonicalName();

    public static void show(FragmentActivity activity, IAccountImport accountImport) {
        ImportAccountsDialogFragment selectDialogFragment = newInstance(accountImport);
        selectDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
    }

    private static ImportAccountsDialogFragment newInstance(IAccountImport accountImport) {
        return new ImportAccountsDialogFragment(accountImport);
    }

    @SuppressLint("ValidFragment")
    public ImportAccountsDialogFragment(IAccountImport accountImportCallback) {
        this.accountImportCallback = accountImportCallback;
        this.accountImporter = new AccountImporter();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.import_accounts_fragment, null);
        lvAccounts = (ListView) view.findViewById(R.id.lvAccounts);
        progressBar = (ProgressBar) view.findViewById(R.id.pbProgress);
        lvAccounts.setItemsCanFocus(false);
        lvAccounts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.import_account_dialog_title)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int x) {
                        // Notify listener about registered account
                        accountImportCallback.accountAccessGranted(getSelectedSingleAccount());
                    }
                })
                .setNeutralButton("New account", null) // Click handler see onNewCreateAccountShowListener!
                .setNegativeButton(android.R.string.no, null)
                .create();

        dialog.setOnShowListener(onNewCreateAccountShowListener);

        return dialog;
    }

    private DialogInterface.OnShowListener onNewCreateAccountShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(final DialogInterface dialog) {
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    accountImporter.addNewAccount();

                    /*
                    if(accountImporter.addNewAccount()) {
                        //Dismiss once everything is OK.
                        dialog.dismiss();
                    }*/
                }
            });
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        accountImporter.onStart(getActivity(), accountsReceivedCallback);
    }

    @Override
    public void onStop() {
        accountImporter.onStop();
        super.onStop();
    }

    private SingleAccount getSelectedSingleAccount() {
        SingleAccount account = null;
        for (int i = 0; i < lvAccounts.getAdapter().getCount(); i++) {
            if (lvAccounts.getChildAt(i) instanceof Checkable && ((Checkable) lvAccounts.getChildAt(i)).isChecked()) {
                account = (SingleAccount) lvAccounts.getAdapter().getItem(i);
                break;
            }
        }
        return account;
    }


    private IAccountsReceived accountsReceivedCallback = new IAccountsReceived() {
        @Override
        public void accountsReceived(ArrayList<HashMap<String, String>> accounts) {
            List<SingleAccount> accountList = new ArrayList<>();

            for (HashMap<String, String> account : accounts) {
                String username = account.get("username");
                String password = account.get("password");
                String url = account.get("url");
                accountList.add(new SingleAccount(username, password, url));
            }

            progressBar.setVisibility(View.GONE);
            lvAccounts.setAdapter(new AccountImporterAdapter(getActivity(), accountList.toArray(new SingleAccount[accountList.size()]), lvAccounts));
        }
    };
}
