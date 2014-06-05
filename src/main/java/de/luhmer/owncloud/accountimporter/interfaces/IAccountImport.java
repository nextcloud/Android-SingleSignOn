package de.luhmer.owncloud.accountimporter.interfaces;

import android.accounts.Account;
import android.os.Bundle;

import de.luhmer.owncloud.accountimporter.helper.OwnCloudAccount;

/**
 * Created by David on 28.05.2014.
 */
public interface IAccountImport {
    //public void accountAccessGranted(Account account, Bundle data);

    public void accountAccessGranted(OwnCloudAccount account);
}
