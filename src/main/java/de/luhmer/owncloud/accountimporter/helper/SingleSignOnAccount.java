package de.luhmer.owncloud.accountimporter.helper;

/**
 * Created by david on 15.06.17.
 */

public class SingleSignOnAccount {

    public SingleSignOnAccount(String name, String username, String password, String url, Boolean disableHostnameVerification) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.url = url;
        this.disableHostnameVerification = disableHostnameVerification;
    }

    public String name; // Name of the account in android
    public String username;
    public String password;
    public String url;
    public Boolean disableHostnameVerification;

}
