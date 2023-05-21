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

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.nextcloud.android.sso.Constants;

import java.util.Optional;

public class SSOException extends Exception {

    @Nullable
    @StringRes
    protected final Integer titleRes;
    @Nullable
    @StringRes
    protected final Integer actionTextRes;
    @Nullable
    protected final Intent actionIntent;

    public SSOException() {
        this(null);
    }

    public SSOException(String message) {
        this(message, null);
    }

    public SSOException(String message, @Nullable @StringRes Integer titleRes) {
        this(message, titleRes, null, null);
    }

    public SSOException(String message,
                        @Nullable @StringRes Integer titleRes,
                        @Nullable Throwable cause) {
        this(message, titleRes, null, null, cause);
    }

    public SSOException(String message,
                        @Nullable @StringRes Integer titleRes,
                        @Nullable @StringRes Integer actionTextRes,
                        @Nullable Intent actionIntent) {
        this(message, titleRes, actionTextRes, actionIntent, null);
    }

    public SSOException(String message,
                        @Nullable @StringRes Integer titleRes,
                        @Nullable @StringRes Integer actionTextRes,
                        @Nullable Intent actionIntent,
                        @Nullable Throwable cause) {
        super(message, cause);
        this.titleRes = titleRes;
        this.actionTextRes = actionTextRes;
        this.actionIntent = actionIntent;
    }

    public Optional<Integer> getTitleRes() {
        return Optional.ofNullable(titleRes);
    }

    public Optional<Integer> getPrimaryActionTextRes() {
        return Optional.ofNullable(actionTextRes);
    }

    public Optional<Intent> getPrimaryAction() {
        return Optional.ofNullable(actionIntent);
    }

    public static SSOException parseNextcloudCustomException(@NonNull Context context, @Nullable Exception exception) {
        if (exception == null) {
            return new UnknownErrorException("Parsed exception is null");
        }

        final String message = exception.getMessage();
        if (message == null) {
            return new UnknownErrorException("Exception message is null");
        }

        switch (message) {
            case Constants.EXCEPTION_INVALID_TOKEN:
                return new TokenMismatchException(context);
            case Constants.EXCEPTION_ACCOUNT_NOT_FOUND:
                return new NextcloudFilesAppAccountNotFoundException(context);
            case Constants.EXCEPTION_UNSUPPORTED_METHOD:
                return new NextcloudUnsupportedMethodException(context);
            case Constants.EXCEPTION_INVALID_REQUEST_URL:
                return new NextcloudInvalidRequestUrlException(context, exception.getCause());
            case Constants.EXCEPTION_HTTP_REQUEST_FAILED:
                final int statusCode = Integer.parseInt(exception.getCause().getMessage());
                final var cause = exception.getCause().getCause();
                return new NextcloudHttpRequestFailedException(context, statusCode, cause);
            case Constants.EXCEPTION_ACCOUNT_ACCESS_DECLINED:
                return new NextcloudFilesAppAccountPermissionNotGrantedException(context);
            default:
                return new UnknownErrorException(exception.getMessage());
        }
    }
}
