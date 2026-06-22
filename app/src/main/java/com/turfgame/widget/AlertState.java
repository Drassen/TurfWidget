package com.turfgame.widget;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tracks the "zone lost" alert across updates. This replaces the global,
 * mutable static fields that used to live in {@code CharStats}; the state is
 * now persisted so it survives the process being recreated between updates.
 *
 * <p>The previous zone count only ever advances upwards. Once the current
 * count drops below it the widget is "alerting" until the user acknowledges
 * it (which calls {@link #reset(Context)}).
 */
final class AlertState {

    private static final String PREFS = "turf_alert_state";
    private static final String KEY_PREV_ZONES = "prev_zones";
    private static final String KEY_ALERTING = "alerting";

    private AlertState() {
    }

    /** Outcome of comparing the latest zone count against the stored baseline. */
    static final class Evaluation {
        final boolean zonesLost;
        /** True only on the update that first entered the alerting state. */
        final boolean firstAlert;
        final int lostCount;

        Evaluation(boolean zonesLost, boolean firstAlert, int lostCount) {
            this.zonesLost = zonesLost;
            this.firstAlert = firstAlert;
            this.lostCount = lostCount;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static Evaluation evaluate(Context context, int currentZones) {
        SharedPreferences prefs = prefs(context);
        int previousZones = prefs.getInt(KEY_PREV_ZONES, -1);
        boolean wasAlerting = prefs.getBoolean(KEY_ALERTING, false);

        boolean zonesLost;
        int lostCount = 0;
        if (previousZones < 0 || currentZones >= previousZones) {
            previousZones = currentZones;
            zonesLost = false;
        } else {
            zonesLost = true;
            lostCount = previousZones - currentZones;
        }

        boolean firstAlert = zonesLost && !wasAlerting;

        prefs.edit()
                .putInt(KEY_PREV_ZONES, previousZones)
                .putBoolean(KEY_ALERTING, zonesLost)
                .apply();

        return new Evaluation(zonesLost, firstAlert, lostCount);
    }

    /** Clears the alert so the next update re-establishes a baseline. */
    static void reset(Context context) {
        prefs(context).edit()
                .putInt(KEY_PREV_ZONES, -1)
                .putBoolean(KEY_ALERTING, false)
                .apply();
    }
}
