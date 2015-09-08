package com.ru.dots.dotsproj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {

    private Vibrator m_vibrator;
    private Boolean m_use_vibrator = false;
    private Boolean m_have_sound = false;
    private int m_dotsCount = 6;
    SharedPreferences m_sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        m_vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        m_sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        m_use_vibrator = m_sp.getBoolean("vibrate", false);
        m_have_sound = m_sp.getBoolean("sound", false);
        //m_dotsCount = m_sp.getInt("dotsCount", 6);
    }

    @Override
    protected void onStart() {
        super.onStart();
        m_use_vibrator = m_sp.getBoolean("vibrate", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void GoToScores(View v){
        Intent i = new Intent(this, HiscoreActivity.class);
        // set temporary dummy data
        TinyDB db = new TinyDB(this);
        db.putListInt("Scores", new ArrayList<Integer>(Arrays.asList(10, 20, 30, 40 , 50)));
        // start activity
        startActivity(i);
    }

    public void viewSettings(View v){
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }
}
