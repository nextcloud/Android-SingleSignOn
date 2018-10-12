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

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.nextcloud.android.sso.Constants;
import com.nextcloud.android.sso.model.ExceptionMessage;

public class SSOException extends Exception {

    private static final String TAG = SSOException.class.getCanonicalName();
    protected ExceptionMessage em;

    public SSOException() {
        super("Single Sign On Exception (use getMessage(context) for more information)");
    }

    public void loadExceptionMessage(Context context) {
        this.em = new ExceptionMessage(
                "Unknown error",
                "Unknown error.."
        );
    }

    public String getTitle(Context context) {
        if (em == null) {
            loadExceptionMessage(context);
        }
        return em.title;
    }

    public String getMessage(Context context) {
        if (em == null) {
            loadExceptionMessage(context);
        }
        return em.message;
    }

    @Override
    public String getMessage() {
        // If already loaded.. return it
        if (em != null) {
            return em.message;
        }

        // Otherwise try to get the application via reflection
        Application app = getApplication();
        if (app != null) {
            loadExceptionMessage(app);
            return em.message;
        }

        // If that didn't work.. well return the "generic" base message
        return super.getMessage();
    }

    private Application getApplication() {
        try {
            return (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }


    public static SSOException parseNextcloudCustomException(Exception exception) {
        switch (exception.getMessage()) {
            case Constants.EXCEPTION_INVALID_TOKEN:
                return new TokenMismatchException();
            case Constants.EXCEPTION_ACCOUNT_NOT_FOUND:
                return new NextcloudFilesAppAccountNotFoundException();
            case Constants.EXCEPTION_UNSUPPORTED_METHOD:
                return new NextcloudUnsupportedMethodException();
            case Constants.EXCEPTION_INVALID_REQUEST_URL:
                return new NextcloudInvalidRequestUrlException(exception.getCause().getMessage());
            case Constants.EXCEPTION_HTTP_REQUEST_FAILED:
                int statusCode = Integer.parseInt(exception.getCause().getMessage());
                Throwable cause = exception.getCause().getCause();
                return new NextcloudHttpRequestFailedException(statusCode, cause);
            case Constants.EXCEPTION_ACCOUNT_ACCESS_DECLINED:
                return new NextcloudFilesAppAccountPermissionNotGrantedException();
            default:
                return new UnknownErrorException(exception.getMessage());
        }
    }
}
