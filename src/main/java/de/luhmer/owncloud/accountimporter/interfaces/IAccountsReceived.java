package de.luhmer.owncloud.accountimporter.interfaces;

import android.accounts.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by david on 15.06.17.
 */

public interface IAccountsReceived {

    void accountsReceived(List<Account> accounts);

}
