package de.luhmer.owncloud.accountimporter.helper;

import java.util.regex.Pattern;

import de.luhmer.owncloud.accountimporter.ImportAccountsDialogFragment;

/**
 * Created by David on 05.06.2014.
 */
public class OwnCloudAccount {
    String url;
    String username;
    String password;

    public OwnCloudAccount(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = validateURL(url);
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    static final Pattern RemoveAllDoubleSlashes = Pattern.compile("(?<!:)\\/\\/");
    public static String validateURL(String url) {
        return RemoveAllDoubleSlashes.matcher(url).replaceAll("/");
    }
}
