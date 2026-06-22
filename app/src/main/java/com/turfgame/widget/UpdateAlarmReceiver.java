package com.turfgame.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives the scheduled-update alarm and hands the actual work off to
 * {@link UpdateWorker}. Scheduled updates may vibrate/notify on a zone loss.
 */
public class UpdateAlarmReceiver extends BroadcastReceiver {

    static final String ACTION_SCHEDULED_UPDATE = "com.turfgame.widget.action.SCHEDULED_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_SCHEDULED_UPDATE.equals(intent.getAction())) {
            UpdateWorker.enqueue(context, true);
        }
    }
}
