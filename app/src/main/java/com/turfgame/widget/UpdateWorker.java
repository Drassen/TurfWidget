package com.turfgame.widget;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.turfgame.widget.model.CharStats;
import com.turfgame.widget.net.TurfApi;
import com.turfgame.widget.net.TurfApiException;
import com.turfgame.widget.render.WidgetRenderer;

/**
 * Performs a single widget refresh on a background thread managed by
 * WorkManager. Using WorkManager (instead of the old started {@code Service})
 * avoids the background-service-start restrictions on Android 8+ and
 * guarantees the work runs to completion off the main thread.
 */
public class UpdateWorker extends Worker {

    private static final String WORK_NAME = "turf_widget_update";
    private static final String KEY_VIBRATE = "vibrate";

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Enqueues a refresh. Rapidly repeated requests coalesce via
     * {@link ExistingWorkPolicy#REPLACE} so a burst of taps can't pile up.
     *
     * @param vibrate whether this update is allowed to vibrate/notify on a
     *                newly detected zone loss (only scheduled updates do).
     */
    static void enqueue(Context context, boolean vibrate) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UpdateWorker.class)
                .setInputData(new Data.Builder().putBoolean(KEY_VIBRATE, vibrate).build())
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        boolean vibrate = getInputData().getBoolean(KEY_VIBRATE, false);

        WidgetRenderer.showLoading(context);

        try {
            CharStats stats = TurfApi.fetchUser(Settings.email(context));
            AlertState.Evaluation alert = AlertState.evaluate(context, stats.zones());

            if (alert.firstAlert && vibrate && Settings.vibrate(context) && !isSilent(context)) {
                vibrate(context);
            }

            WidgetRenderer.showStats(context, stats, alert.zonesLost);

            if (alert.zonesLost && Settings.notify(context)) {
                Notifications.showZoneLost(context, alert.lostCount);
            } else {
                Notifications.cancel(context);
            }
        } catch (TurfApiException e) {
            WidgetRenderer.showError(context, e.reason());
        } finally {
            // Always (re)arm the next scheduled refresh, even after an error.
            UpdateScheduler.schedule(context);
        }

        return Result.success();
    }

    private static boolean isSilent(Context context) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audio != null && audio.getRingerMode() == AudioManager.RINGER_MODE_SILENT;
    }

    private static void vibrate(Context context) {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager =
                    (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = manager != null ? manager.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(600);
        }
    }
}
