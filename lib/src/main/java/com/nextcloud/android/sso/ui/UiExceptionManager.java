package com.nextcloud.android.sso.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nextcloud.android.sso.R;
import com.nextcloud.android.sso.exceptions.SSOException;

/**
 * Nextcloud SingleSignOn
 *
 * @author David Luhmer
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public final class UiExceptionManager {

    private static final int NOTIFICATION_ID = 0;
    private static final String CHANNEL_ID = "0";

    private UiExceptionManager() {
    }

    public static void showDialogForException(@NonNull Context context,
                                              @NonNull SSOException exception) {
        final int actionText = exception.getPrimaryActionTextRes().orElse(android.R.string.yes);
        final var optionalAction = exception.getPrimaryAction();

        if (optionalAction.isPresent()) {
            showDialogForException(context, exception, actionText, (dialog, which) -> context.startActivity(optionalAction.get()));
        } else {
            showDialogForException(context, exception, actionText, null);
        }
    }

    /**
     * Overrides {@link SSOException#getPrimaryAction()} with a custom callback.
     */
    public static void showDialogForException(@NonNull Context context,
                                              @NonNull SSOException exception,
                                              @StringRes int actionText,
                                              @Nullable DialogInterface.OnClickListener callback) {
        final var builder = new MaterialAlertDialogBuilder(context)
                .setMessage(exception.getMessage());

        exception.getTitleRes().ifPresent(builder::setTitle);

        if (callback == null) {
            builder.setPositiveButton(R.string.close, null);
        } else {
            builder.setPositiveButton(actionText, callback);
            builder.setNeutralButton(R.string.close, null);
        }

        builder.create().show();
    }

    public static void showNotificationForException(@NonNull Context context,
                                                    @NonNull SSOException exception) {
        final String message = exception.getMessage();

        final var builder = new NotificationCompat.Builder(context, "")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setTicker(message)
                //.setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentText(message);

        exception.getTitleRes()
                .map(context::getString)
                .ifPresent(builder::setContentTitle);


        //Intent notificationIntent = new Intent(context, NewsReaderListActivity.class);
        //PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //builder.setContentIntent(contentIntent);

        // Add as notification
        final var notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            final var channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            //mChannel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(CHANNEL_ID);
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
