/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2018-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2023 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2018-2019 David Luhmer <david-dev@live.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nextcloud.android.sso.R;
import com.nextcloud.android.sso.exceptions.SSOException;

import java.util.function.Consumer;

public final class UiExceptionManager {

    private static final int NOTIFICATION_ID = 0;
    private static final String CHANNEL_ID = "0";

    private UiExceptionManager() {
    }

    public static void showDialogForException(@NonNull Context context,
                                              @NonNull SSOException exception) {
        showDialogForException(context, exception, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void showDialogForException(@NonNull Context context,
                                              @NonNull SSOException exception,
                                              @Nullable Consumer<SSOException> onDismiss) {
        final int actionText = exception.getPrimaryActionTextRes().orElse(android.R.string.yes);
        final var optionalAction = exception.getPrimaryAction();

        if (optionalAction.isPresent()) {
            showDialogForException(context, exception, actionText, (dialog, which) -> context.startActivity(optionalAction.get()), onDismiss);
        } else {
            showDialogForException(context, exception, actionText, null, onDismiss);
        }
    }

    /**
     * Overrides {@link SSOException#getPrimaryAction()} with a custom callback.
     */
    public static void showDialogForException(@NonNull Context context,
                                              @NonNull SSOException exception,
                                              @StringRes int actionText,
                                              @Nullable DialogInterface.OnClickListener callback) {
        showDialogForException(context, exception, actionText, callback, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void showDialogForException(@NonNull Context context,
                                              @NonNull SSOException exception,
                                              @StringRes int actionText,
                                              @Nullable DialogInterface.OnClickListener callback,
                                              @Nullable Consumer<SSOException> onDismiss) {
        final var builder = new MaterialAlertDialogBuilder(context)
            .setMessage(exception.getMessage())
            .setOnDismissListener(d -> {
                if (onDismiss != null) {
                    onDismiss.accept(exception);
                }
            });

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
