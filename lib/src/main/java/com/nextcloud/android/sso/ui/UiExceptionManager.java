package com.nextcloud.android.sso.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nextcloud.android.sso.R;
import com.nextcloud.android.sso.exceptions.NextcloudApiNotRespondingException;
import com.nextcloud.android.sso.exceptions.SSOException;

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

public final class UiExceptionManager {

    private static final int NOTIFICATION_ID = 0;
    private static final String CHANNEL_ID = "0";

    private UiExceptionManager() { }

    public static void showDialogForException(Context context, SSOException exception) {
        showDialogForException(context, exception, null);
    }

    public static void showDialogForException(Context context, SSOException exception, DialogInterface.OnClickListener callback) {
        // Enable hyperlinks in message
        final SpannableString message = new SpannableString(exception.getMessage(context));
        Linkify.addLinks(message, Linkify.ALL);

        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setTitle(exception.getTitle(context))
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, callback)
                .setIcon(android.R.drawable.ic_dialog_alert);


        if (exception instanceof NextcloudApiNotRespondingException && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            dialogBuilder.setNegativeButton(context.getString(R.string.nextcloud_files_api_not_responding_open_battery_optimization_settings), (dialogInterface, i) -> {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                context.startActivity(intent);
            });
        }

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Make the textview clickable. Must be called after show()
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void showNotificationForException(Context context, SSOException exception) {
        String title = exception.getTitle(context);
        String message = exception.getMessage(context);
        String tickerMessage = message;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "")
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setTicker(tickerMessage)
                        .setContentTitle(title)
                        //.setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentText(message);


        //Intent notificationIntent = new Intent(context, NewsReaderListActivity.class);
        //PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            //mChannel.enableLights(true);
            manager.createNotificationChannel(mChannel);
            builder.setChannelId(CHANNEL_ID);
        }

        manager.notify(NOTIFICATION_ID, builder.build());
    }
}
