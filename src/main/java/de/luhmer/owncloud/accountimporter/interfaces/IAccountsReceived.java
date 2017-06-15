package de.luhmer.owncloud.accountimporter.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by david on 15.06.17.
 */

public interface IAccountsReceived {

    void accountsReceived(ArrayList<HashMap<String, String>> accounts);

}
