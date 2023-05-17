/*
 *  Nextcloud SingleSignOn
 *
 *  @author David Luhmer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextcloud.android.sso.exceptions;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nextcloud.android.sso.Constants;
import com.nextcloud.android.sso.R;
import com.nextcloud.android.sso.model.ExceptionMessage;

import java.util.Optional;

public class SSOException extends Exception {

    private static final String TAG = SSOException.class.getCanonicalName();
    @Nullable
    protected ExceptionMessage em;

    public SSOException() {
        super("Single Sign On Exception (use getMessage(context) for more information)");
    }

    @Nullable
    public String getTitle(@NonNull Context context) {
        return loadExceptionMessage()
                .map(em -> em.message)
                .orElse(null);
    }

    public String getMessage(@NonNull Context context) {
        return getMessage();
    }

    @Override
    public String getMessage() {
        return loadExceptionMessage()
                .map(em -> em.message)
                .orElseGet(super::getMessage);
    }

    public Optional<Integer> getPrimaryActionText() {
        return loadExceptionMessage()
                .map(em -> em.actionText);
    }

    public Optional<Intent> getPrimaryAction() {
        return loadExceptionMessage()
                .map(em -> em.actionIntent);
    }

    private Optional<ExceptionMessage> loadExceptionMessage() {
        if (this.em == null) {
            final var app = getApplication();
            if (app != null) {
                loadExceptionMessage(app);
            }
        }

        return Optional.ofNullable(em);
    }

    public void loadExceptionMessage(@NonNull Context context) {
        this.em = new ExceptionMessage(
                context.getString(R.string.unknown_error_title),
                context.getString(R.string.unknown_error_message)
        );
    }

    @SuppressLint("PrivateApi")
    private Application getApplication() {
        try {
            return (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            final String message = e.getMessage();
            Log.e(TAG, message == null ? e.getClass().getSimpleName() : message);
        }
        return null;
    }


    public static SSOException parseNextcloudCustomException(@Nullable Exception exception) {
        if (exception == null) {
            return new UnknownErrorException("Parsed exception is null");
        }

        final String message = exception.getMessage();
        if (message == null) {
            return new UnknownErrorException("Exception message is null");
        }

        switch (message) {
            case Constants.EXCEPTION_INVALID_TOKEN:
                return new TokenMismatchException();
            case Constants.EXCEPTION_ACCOUNT_NOT_FOUND:
                return new NextcloudFilesAppAccountNotFoundException();
            case Constants.EXCEPTION_UNSUPPORTED_METHOD:
                return new NextcloudUnsupportedMethodException();
            case Constants.EXCEPTION_INVALID_REQUEST_URL:
                return new NextcloudInvalidRequestUrlException(exception.getCause().getMessage());
            case Constants.EXCEPTION_HTTP_REQUEST_FAILED:
                final int statusCode = Integer.parseInt(exception.getCause().getMessage());
                final var cause = exception.getCause().getCause();
                return new NextcloudHttpRequestFailedException(statusCode, cause);
            case Constants.EXCEPTION_ACCOUNT_ACCESS_DECLINED:
                return new NextcloudFilesAppAccountPermissionNotGrantedException();
            default:
                return new UnknownErrorException(exception.getMessage());
        }
    }
}
