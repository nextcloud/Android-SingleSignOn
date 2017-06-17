package de.luhmer.owncloud.accountimporter.helper;

/**
 * Created by david on 15.06.17.
 */

public class SingleAccount {

    public SingleAccount(String username, String password, String url, Boolean disableHostnameVerification) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.disableHostnameVerification = disableHostnameVerification;
    }

    public String username;
    public String password;
    public String url;
    public Boolean disableHostnameVerification;

}