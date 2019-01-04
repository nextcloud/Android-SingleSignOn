package com.nextcloud.android.sso.exceptions;

import android.content.Context;

import com.nextcloud.android.sso.R;
import com.nextcloud.android.sso.model.ExceptionMessage;

public class NextcloudApiNotRespondingException extends SSOException {

    @Override
    public void loadExceptionMessage(Context context) {
        this.em = new ExceptionMessage(
                context.getString(R.string.nextcloud_files_api_not_responsing_title),
                context.getString(R.string.nextcloud_files_api_not_responsing_message)
        );
    }

}
