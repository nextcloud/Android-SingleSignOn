package com.nextcloud.android.sso.exceptions;

import android.content.Context;

import com.nextcloud.android.sso.R;
import com.nextcloud.android.sso.model.ExceptionMessage;

public class AndroidGetAccountsPermissionNotGranted extends SSOException {

    @Override
    public void loadExceptionMessage(Context context) {
        this.em = new ExceptionMessage(
             context.getString(R.string.android_get_accounts_permission_not_granted_exception_title),
             context.getString(R.string.android_get_accounts_permission_not_granted_exception_message)
        );
    }
}
