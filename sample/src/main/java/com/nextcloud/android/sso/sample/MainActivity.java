package com.nextcloud.android.sso.sample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.NextcloudRetrofitApiBuilder;

@SuppressLint("SetTextI18n")
@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.chooseAccountBtn).setOnClickListener(v -> {
            try {
                /*
                 * Prompt dialog to select existing or create a new account
                 * As soon as an account has been imported, we will continue in #onActivityResult()
                 */
                AccountImporter.pickNewAccount(this);
            } catch (NextcloudFilesAppNotInstalledException | AndroidGetAccountsPermissionNotGranted e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            AccountImporter.onActivityResult(requestCode, resultCode, data, this, ssoAccount -> {
                Log.i(TAG, "Imported account: " + ssoAccount.name);
                SingleAccountHelper.setCurrentAccount(this, ssoAccount.name);
                executor.submit(() -> {

                    // Create local bridge API to the Nextcloud Files Android app
                    final var nextcloudAPI = createNextcloudAPI(ssoAccount);

                    // Create the Ocs API to talk to the server
                    final var ocsAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, "ocs/v2.php/cloud/").create(OcsAPI.class);

                    try {
                        // Perform actual requests
                        final var userName = ocsAPI.getUser(ssoAccount.userId).execute().body().ocs.data.displayName;
                        final var instanceName = ocsAPI.getCapabilities().execute().body().ocs.data.theming.name;

                        ((TextView) findViewById(R.id.result)).setText(userName + " on " + instanceName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    nextcloudAPI.stop();
                });
            });
        } catch (AccountImportCancelledException e) {
            Log.i(TAG, "Account import cancelled.");
        }
    }

    private NextcloudAPI createNextcloudAPI(@NonNull SingleSignOnAccount ssoAccount) {
        return new NextcloudAPI(this, ssoAccount, new GsonBuilder().create(), new NextcloudAPI.ApiConnectedListener() {
            @Override
            public void onConnected() {
                /*
                 * We don't have to wait for this callback because requests are queued and executed
                 * automatically as soon as the connection has been established.
                 */
                Log.i(TAG, "SSO API connected for " + ssoAccount);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}