package de.luhmer.owncloud.accountimporter.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;

import de.luhmer.owncloud.accountimporter.exceptions.SSOException;

public class UiExceptionManager {

    public static void ShowDialogForException(Context context, SSOException exception) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(exception.getTitle(context))
                .setMessage(exception.getMessage(context))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private static final int NOTIFICATION_ID = 0;
    private static final String CHANNEL_ID = "0";

    public static void ShowNotificationForException(Context context, SSOException exception) {
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
