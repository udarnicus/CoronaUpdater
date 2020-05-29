package com.example.coronaupdater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private ArrayList<RowItem> singleRow;
    private LayoutInflater thisInflater;


    public CustomAdapter(Context context, ArrayList<RowItem> aRow) {

        this.singleRow = aRow;
        thisInflater = ( LayoutInflater.from(context) );

    }

    @Override
    public int getCount() {
        return singleRow.size();
    }

    @Override
    public Object getItem(int position) {
        return singleRow.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = thisInflater.inflate( R.layout.listview_row, parent, false );
            TextView toteZahlTextView = convertView.findViewById(R.id.toteZahlTextView);
            TextView infizierteZahlTextView = convertView.findViewById(R.id.infizierteZahlTextView);
            ImageView flag = convertView.findViewById(R.id.flagView);

            RowItem currentRow = (RowItem) getItem(position);

            toteZahlTextView.setText(currentRow.getTote());
            infizierteZahlTextView.setText(currentRow.getInfizierte());
            flag.setImageResource(currentRow.getFlag());
        }


        return convertView;
    }
}
