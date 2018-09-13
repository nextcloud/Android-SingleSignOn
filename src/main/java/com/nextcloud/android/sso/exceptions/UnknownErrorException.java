package com.nextcloud.android.sso.exceptions;

import android.content.Context;

import com.nextcloud.android.sso.model.ExceptionMessage;

public class UnknownErrorException extends SSOException {

    public UnknownErrorException() {
        super();
    }

    public UnknownErrorException(String message) {
        super();
        this.em = new ExceptionMessage("", message);
    }

    @Override
    public void loadExceptionMessage(Context context) {
        if(this.em == null) {
            super.loadExceptionMessage(context);
        }
    }
}
