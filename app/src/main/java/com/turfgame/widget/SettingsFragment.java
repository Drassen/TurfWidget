package com.turfgame.widget;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

/**
 * Inflates the preference screen. The preference keys are unchanged, so any
 * existing user settings are preserved.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
