package com.ru.dots.dotsproj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by eddadr on 14.9.2015.
 */
public class RecordAdapter extends ArrayAdapter<Record> {

    private final Context context;
    private final List<Record> values;

    public RecordAdapter(Context context, List<Record> objects) {
        super(context, -1, objects);
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout, parent,false);

        TextView nrView = (TextView) rowView.findViewById(R.id.row_nr);
        nrView.setText( values.get(position).getNumber());

        TextView dateView = (TextView) rowView.findViewById(R.id.row_date);
        dateView.setText( values.get(position).getDate());

        TextView scoreView = (TextView) rowView.findViewById(R.id.row_score);
        scoreView.setText(String.valueOf(values.get(position).getScore()));

        return rowView;
    }
}
