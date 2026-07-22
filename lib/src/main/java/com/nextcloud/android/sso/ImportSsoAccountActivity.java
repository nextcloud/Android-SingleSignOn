/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.exceptions.AccountImportCancelledException;
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted;
import com.nextcloud.android.sso.exceptions.SSOException;
import com.nextcloud.android.sso.ui.UiExceptionManager;

/// This is a Trampoline Activity that takes care about requesting all necessary permissions and guiding the user through the complete import flow.
/// It is extremely important to catch *any* error and finish this activity because otherwise, the Trampoline Activity will stay on top but transparent, so users can't see it and users can't interact with their (visible) 3rd party app anymore.
public class ImportSsoAccountActivity extends AppCompatActivity {

    private static final String TAG = ImportSsoAccountActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {

            try {
                try {
                    AccountImporter.checkAndroidAccountPermissions(this);
                    //noinspection removal
                    AccountImporter.pickNewAccount(this);

                } catch (AndroidGetAccountsPermissionNotGranted e) {
                    //noinspection removal
                    AccountImporter.requestAndroidAccountPermissionsAndPickAccount(this);
                }

            } catch (Throwable throwable) {
                if (throwable instanceof SSOException ssoException) {
                    UiExceptionManager.showDialogForException(this, ssoException, t -> finish());
                } else {
                    throwable.printStackTrace();
                    finish();
                }
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this, null, t -> finish());
        } catch (Throwable throwable) {
            if (throwable instanceof SSOException ssoException) {
                UiExceptionManager.showDialogForException(this, ssoException, t -> finish());
            } else {
                throwable.printStackTrace();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            AccountImporter.onActivityResult(requestCode, resultCode, data, this, null, ssoAccount -> {
                final var result = new Intent();
                result.putExtra(Constants.NEXTCLOUD_SSO, ssoAccount);
                setResult(RESULT_OK, result);
                finish();
            }, t -> finish());

        } catch (AccountImportCancelledException e) {
            Log.i(TAG, "Account import cancelled.");
            setResult(RESULT_CANCELED);
            finish();
        } catch (Throwable t) {
            if (t instanceof SSOException ssoException) {
                UiExceptionManager.showDialogForException(this, ssoException, t1 -> finish());
            } else {
                t.printStackTrace();
                finish();
            }
        }
    }
}
