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
import android.view.View;
import android.widget.Checkable;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloud.accountimporter.adapter.AccountImporterAdapter;
import de.luhmer.owncloud.accountimporter.helper.AccountImporter;
import de.luhmer.owncloud.accountimporter.interfaces.IAccountImport;


/**
 * Created by David on 16.05.2014.
 */
@SuppressLint("ValidFragment")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ImportAccountsDialogFragment extends DialogFragment {

    public IAccountImport accountImport;

    public static void show(FragmentActivity activity, IAccountImport accountImport) {
        ImportAccountsDialogFragment selectDialogFragment = newInstance(accountImport);
        selectDialogFragment.show(activity.getSupportFragmentManager(), "dialog");
    }

    private static ImportAccountsDialogFragment newInstance(IAccountImport accountImport) {
        return new ImportAccountsDialogFragment(accountImport);
    }

    @SuppressLint("ValidFragment")
    public ImportAccountsDialogFragment(IAccountImport accountImport) {
        this.accountImport = accountImport;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.import_accounts_fragment, null);

        lvAccounts = (ListView) view.findViewById(R.id.lvAccounts);

        final List<Account> accounts = AccountImporter.findAccounts(getActivity());
        List<AccountImporterAdapter.SingleAccount> accountList = new ArrayList<AccountImporterAdapter.SingleAccount>();
        for(Account account : accounts) {
            accountList.add(new AccountImporterAdapter.SingleAccount(account.type, account.name, false));
        }

        lvAccounts.setAdapter(new AccountImporterAdapter(getActivity(), accountList.toArray(new AccountImporterAdapter.SingleAccount[accountList.size()]), lvAccounts));

        lvAccounts.setItemsCanFocus(false);
        lvAccounts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.import_account_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int x) {

                        for (int i = 0; i < lvAccounts.getAdapter().getCount(); i++) {
                            if (lvAccounts.getChildAt(i) instanceof Checkable && ((Checkable) lvAccounts.getChildAt(i)).isChecked()) {

                                AccountImporter.getAuthTokenForAccount(getActivity(), accounts.get(i), accountImport);

                                /*
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                intent.putExtra(LoginActivity.NEW_ACCOUNT, true);
                                intent.putExtra(URL_STRING, calendars.get(i));
                                intent.putExtra(USERNAME_STRING, username);
                                intent.putExtra(PASSWORD_STRING, password);
                                startActivity(intent);
                                */
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
    }

    ListView lvAccounts;



}
