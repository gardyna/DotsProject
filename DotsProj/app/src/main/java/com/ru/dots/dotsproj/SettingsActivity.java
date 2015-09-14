package com.ru.dots.dotsproj;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by eddadr on 7.9.2015.
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String VIBRATE = "vibrate";
    public static final String DOTSCOUNT = "dotsCount";
    public static final String SOUND = "sound";
    public static final String RESETHIGHSCORE = "resetHighScore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Get the custom preference
        Preference resetButton = (Preference) findPreference(getString(R.string.resetHighScore));
        resetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getBaseContext(), "The custom preference has been clicked",
                        Toast.LENGTH_LONG).show();

                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Reset high score")
                        .setMessage("Are you sure you want to reset the high score list?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                resetHighScore();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }

        });
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
        else if (key.equals(SOUND)){
            Preference connectionPref = findPreference(key);
            connectionPref.setSummary(sharedPreferences.getString(key, "sound"));
        }
        else if (key.equals(RESETHIGHSCORE)){
            Preference connectionPreference = findPreference(key);
            connectionPreference.setSummary(sharedPreferences.getString(key, "resetHighScore"));
            resetHighScore();
        }
    }

    private void resetHighScore() {
        TinyDB db = new TinyDB(getApplicationContext());
        ArrayList<Integer> scores = db.getListInt("Scores");
        scores.clear();
        db.putListInt("Scores", scores);
    }

}
