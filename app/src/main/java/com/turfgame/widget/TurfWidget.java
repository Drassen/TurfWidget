package com.turfgame.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

/**
 * The home-screen widget provider. It owns the user-facing actions (refresh,
 * launch Turf, acknowledge an alert) and delegates the actual update work to
 * {@link UpdateWorker}.
 */
public class TurfWidget extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "com.turfgame.widget.action.REFRESH";
    public static final String ACTION_LAUNCH_TURF = "com.turfgame.widget.action.LAUNCH_TURF";
    public static final String ACTION_RESET_ALERT = "com.turfgame.widget.action.RESET_ALERT";

    /** Known package names the Turf game has shipped under, newest first. */
    private static final String[] TURF_PACKAGES = {
            "se.turfwars.android",
            "com.andrimon.turf",
            "com.andrimon.turf.android"
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        UpdateWorker.enqueue(context, false);
        UpdateScheduler.schedule(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action)) {
            UpdateWorker.enqueue(context, false);
        } else if (ACTION_LAUNCH_TURF.equals(action)) {
            acknowledgeAlert(context);
            launchTurf(context);
        } else if (ACTION_RESET_ALERT.equals(action)) {
            acknowledgeAlert(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Last widget removed: stop refreshing and clear any pending alert.
        UpdateScheduler.cancel(context);
        Notifications.cancel(context);
        super.onDisabled(context);
    }

    static Intent launchIntent(Context context) {
        return new Intent(context, TurfWidget.class).setAction(ACTION_LAUNCH_TURF);
    }

    static Intent resetAlertIntent(Context context) {
        return new Intent(context, TurfWidget.class).setAction(ACTION_RESET_ALERT);
    }

    private void acknowledgeAlert(Context context) {
        AlertState.reset(context);
        Notifications.cancel(context);
        UpdateWorker.enqueue(context, false);
    }

    private void launchTurf(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent launch = null;
        for (String pkg : TURF_PACKAGES) {
            launch = packageManager.getLaunchIntentForPackage(pkg);
            if (launch != null) {
                break;
            }
        }

        if (launch == null) {
            Toast.makeText(context, R.string.turf_not_installed, Toast.LENGTH_LONG).show();
            return;
        }

        // getLaunchIntentForPackage already targets the app's existing task if
        // it is running, so the deprecated getRunningTasks/moveTaskToFront dance
        // is no longer needed.
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
    }
}
