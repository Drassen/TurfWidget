package com.turfgame.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Schedules the next periodic widget refresh with {@link AlarmManager}.
 *
 * <p>An inexact, allow-while-idle alarm is used: it requires no special
 * permission (unlike exact alarms on Android 12+) and battery-friendly
 * inexactness is perfectly acceptable for a stats widget. Each refresh
 * re-arms the next one, so a single repeating alarm is kept alive while at
 * least one widget exists.
 */
final class UpdateScheduler {

    private UpdateScheduler() {
    }

    static void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent operation = alarmOperation(context);
        long intervalMillis = Settings.updateFreqMillis(context);
        if (intervalMillis <= 0) {
            // "Never" — make sure no stale alarm remains.
            alarmManager.cancel(operation);
            return;
        }

        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, triggerAtMillis, operation);
        } else {
            alarmManager.set(AlarmManager.RTC, triggerAtMillis, operation);
        }
    }

    static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(alarmOperation(context));
        }
    }

    private static PendingIntent alarmOperation(Context context) {
        Intent intent = new Intent(context, UpdateAlarmReceiver.class)
                .setAction(UpdateAlarmReceiver.ACTION_SCHEDULED_UPDATE);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags);
    }
}
