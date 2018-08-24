package com.nextcloud.android.sso.exceptions;

import android.app.Application;
import android.content.Context;

import com.nextcloud.android.sso.R;
import com.nextcloud.android.sso.model.ExceptionMessage;

import java.lang.reflect.InvocationTargetException;

/**
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

public class SSOException extends Exception {

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
        if(em == null) {
            loadExceptionMessage(context);
        }
        return em.title;
    }

    public String getMessage(Context context) {
        if(em == null) {
            loadExceptionMessage(context);
        }
        return em.message;
    }

    @Override
    public String getMessage() {
        // If already loaded.. return it
        if(em != null) {
            return em.message;
        }

        // Otherwise try to get the application via reflection
        Application app = getApplication();
        if(app != null) {
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
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
