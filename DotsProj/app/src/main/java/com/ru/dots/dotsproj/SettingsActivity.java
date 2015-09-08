package com.ru.dots.dotsproj;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by eddadr on 7.9.2015.
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String VIBRATE = "vibrate";
    public static final String DOTSCOUNT = "dotsCount";
    public static final String SOUND = "sound";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(VIBRATE)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(key, "vibrate"));
        } else if (key.equals(DOTSCOUNT)){
            Preference connectionPref = findPreference(key);
            connectionPref.setSummary(sharedPreferences.getString(key, "dotsCount"));
        }
        else
        if (key.equals(SOUND)){
            Preference connectionPref = findPreference(key);
            connectionPref.setSummary(sharedPreferences.getString(key, "sound"));
        }
    }
}
