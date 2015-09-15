package com.ru.dots.dotsproj;

// TODO: set date along with score
// TODO: two lists one for each board size

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HiscoreActivity extends AppCompatActivity {

    private int NUM_CELL;
    private ArrayList<Integer> scores;
    private ListView m_listView;
    ArrayList<Record> m_data = new ArrayList<Record>();
    RecordAdapter m_adapter;
    String m_recordName;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hiscore);
        //TinyDB db = new TinyDB(this);
        //scores = db.getListInt("Scores");
        //Collections.sort(scores, Collections.reverseOrder());
        //ListView v = (ListView)findViewById(R.id.scoreList);
        //ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, scores);
        //v.setAdapter(adapter);

        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        NUM_CELL = Integer.parseInt(sp.getString(SettingsActivity.DOTSCOUNT, "6"));

        if (NUM_CELL == 6)
        {
            m_recordName = "records6.ser";
        } else {
            m_recordName = "records9.ser";
        }

        readRecords();
        m_listView = (ListView) findViewById(R.id.records);
        m_adapter = new RecordAdapter(this, m_data);
        m_adapter.notifyDataSetChanged();
        m_listView.setAdapter(m_adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        //readRecords();
        //m_highscoreRecords.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hiscore, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    void readRecords() {
        try {
            FileInputStream fis = openFileInput(m_recordName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<Record> records = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
            m_data.clear();
            for ( Record rec: records ) {
                m_data.add( rec );
            }
            Collections.sort(m_data, new Comparator<Record>() {
                public int compare(Record o1, Record o2) {
                    return o1.getScore() == o2.getScore() ? 0 : o1.getScore() < o2.getScore() ? -1 : 1;
                }
            });
            Collections.reverse(m_data);
            ArrayList<Record> temp = new ArrayList<Record>();
            int i = 1;
            for ( Record rec: m_data ) {
                rec.setNumber("#" + i);
                temp.add(rec);
                i++;
            }
            m_data = temp;
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
