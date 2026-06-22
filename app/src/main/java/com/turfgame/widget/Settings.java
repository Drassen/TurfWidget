package com.turfgame.widget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

/**
 * Typed, read-only accessors over the user's shared preferences. Centralises
 * the preference keys and their defaults that used to be scattered across the
 * old {@code Prefs} activity.
 */
public final class Settings {

    static final String KEY_VIBRATE = "vibrate";
    static final String KEY_NOTIFY = "notify";
    static final String KEY_LAYOUT = "layout";
    static final String KEY_EMAIL = "user_email";
    static final String KEY_UPDATE_FREQ = "update_freq";

    static final String LAYOUT_DEFAULT = "widget";
    private static final long UPDATE_FREQ_DEFAULT_MS = 1_800_000L; // 30 minutes

    private Settings() {
    }

    private static SharedPreferences prefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static boolean vibrate(Context context) {
        return prefs(context).getBoolean(KEY_VIBRATE, true);
    }

    public static boolean notify(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFY, true);
    }

    public static String layout(Context context) {
        return prefs(context).getString(KEY_LAYOUT, LAYOUT_DEFAULT);
    }

    public static String email(Context context) {
        return prefs(context).getString(KEY_EMAIL, "").trim();
    }

    /**
     * Update interval in milliseconds, or a non-positive value when automatic
     * updates are disabled ("Never").
     */
    public static long updateFreqMillis(Context context) {
        String value = prefs(context).getString(KEY_UPDATE_FREQ, Long.toString(UPDATE_FREQ_DEFAULT_MS));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return UPDATE_FREQ_DEFAULT_MS;
        }
    }
}
