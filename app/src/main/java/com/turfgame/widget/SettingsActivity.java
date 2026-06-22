package com.turfgame.widget;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Hosts the settings UI. Doubles as the widget's configuration activity: when
 * launched as a configure step it returns the widget id on completion. Closing
 * the screen applies the settings by refreshing the widget and rescheduling.
 *
 * <p>Replaces the old {@code Prefs} {@link android.preference.PreferenceActivity}
 * with an AppCompat activity hosting a {@code PreferenceFragmentCompat}.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_POST_NOTIFICATIONS = 1;

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (isConfiguring()) {
            // Default outcome if the user backs out of configuration.
            setResult(RESULT_CANCELED);
        }

        setContentView(R.layout.settings_activity);
        setTitle(R.string.prefs_name);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }

        maybeRequestNotificationPermission();
    }

    @Override
    public void finish() {
        // Apply the (possibly changed) settings: reschedule and refresh now.
        UpdateScheduler.schedule(this);
        UpdateWorker.enqueue(this, false);

        if (isConfiguring()) {
            Intent result = new Intent()
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, result);
        }
        super.finish();
    }

    private boolean isConfiguring() {
        return appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    private void maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_POST_NOTIFICATIONS);
        }
    }
}
