package com.nextcloud.android.sso;

import static com.nextcloud.android.sso.Constants.NEXTCLOUD_SSO;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.model.SingleSignOnAccount;

/**
 * {@link ActivityResultContract} to import a Nextcloud account.
 * The result account may be <code>null</code> if something went wrong or the user canceled the import.
 *
 * @see <a href="https://developer.android.com/training/basics/intents/result"><code>ActivityResultContract</code></a>
 */
public class ImportSsoAccount extends ActivityResultContract<Void, SingleSignOnAccount> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void input) {
        return new Intent(context, ImportSsoAccountActivity.class);
    }

    @Override
    public SingleSignOnAccount parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            return (SingleSignOnAccount) intent.getSerializableExtra(NEXTCLOUD_SSO);
        }

        return null;
    }
}