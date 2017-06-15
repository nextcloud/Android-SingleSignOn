package de.luhmer.owncloud.accountimporter.interfaces;

import de.luhmer.owncloud.accountimporter.helper.SingleAccount;

/**
 * Created by David on 28.05.2014.
 */
public interface IAccountImport {

    void accountAccessGranted(SingleAccount account);

}
