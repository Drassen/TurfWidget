package com.turfgame.widget;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Builds and posts the "zone lost" notification using {@link NotificationCompat}
 * and an O+ notification channel, replacing the long-deprecated
 * {@code Notification.setLatestEventInfo} API.
 */
final class Notifications {

    private static final String CHANNEL_ID = "turf_zone_alerts";
    private static final int NOTIFICATION_ID = 1001;

    private Notifications() {
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.channel_zone_alerts),
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(context.getString(R.string.channel_zone_alerts_desc));
                manager.createNotificationChannel(channel);
            }
        }
    }

    static void showZoneLost(Context context, int lostCount) {
        ensureChannel(context);

        PendingIntent contentIntent = PendingIntent.getBroadcast(
                context, 10, TurfWidget.launchIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());
        PendingIntent deleteIntent = PendingIntent.getBroadcast(
                context, 11, TurfWidget.resetAlertIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.alert_notification)
                .setContentTitle(context.getString(R.string.widget_name))
                .setContentText(context.getString(R.string.ticker_text))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent);
        if (lostCount > 1) {
            builder.setNumber(lostCount);
        }

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // On Android 13+ posting silently no-ops without the runtime permission;
        // guard explicitly to avoid the lint warning and wasted work.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || manager.areNotificationsEnabled()) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    static void cancel(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

    private static int immutableFlag() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
    }
}
