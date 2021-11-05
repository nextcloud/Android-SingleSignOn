package com.nextcloud.android.sso.sample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.GsonBuilder;
import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.NextcloudRetrofitApiBuilder;

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
                 *
                 * In real live applications, you won't import an account on each app start, but remember the imported account via SingleAccountHelper.
                 */
                AccountImporter.pickNewAccount(this);
            } catch (NextcloudFilesAppNotInstalledException | AndroidGetAccountsPermissionNotGranted e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            AccountImporter.onActivityResult(requestCode, resultCode, data, this, ssoAccount -> {
                Log.i(TAG, "Imported account: " + ssoAccount.name);

                /* Network requests need to be performed on a background thread */
                executor.submit(() -> {
                    runOnUiThread(() -> ((TextView) findViewById(R.id.result)).setText(R.string.loading));

                    /* Create local bridge API to the Nextcloud Files Android app */
                    final var nextcloudAPI = new NextcloudAPI(this, ssoAccount, new GsonBuilder().create());

                    /* Create the Ocs API to talk to the server */
                    final var ocsAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, "/ocs/v2.php/cloud/").create(OcsAPI.class);

                    try {
                        /* Perform actual requests */
                        final var user = ocsAPI.getUser(ssoAccount.userId).execute().body().ocs.data;
                        final var serverInfo = ocsAPI.getServerInfo().execute().body().ocs.data;

                        /* Show result on the UI thread */
                        runOnUiThread(() -> ((TextView) findViewById(R.id.result)).setText(
                                getString(R.string.account_info,
                                        user.displayName,
                                        serverInfo.capabilities.theming.name,
                                        serverInfo.version.semanticVersion))
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /*
                     * If you need to make multiple calls, keep the NextcloudAPI open as long as you
                     * can. This way the services will stay active and the connection between the
                     * files app and your app is already established when you make subsequent requests.
                     * Otherwise you'll have to bind to the service again and again for each request.
                     *
                     * @see https://github.com/nextcloud/Android-SingleSignOn/issues/120#issuecomment-540069990
                     */
                    nextcloudAPI.stop();
                });
            });
        } catch (AccountImportCancelledException e) {
            Log.i(TAG, "Account import cancelled.");
        }
    }
}