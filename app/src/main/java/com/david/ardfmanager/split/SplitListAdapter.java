package com.david.ardfmanager.split;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.R;

import java.util.ArrayList;

public class SplitListAdapter extends ArrayAdapter<Split> {

    private Context mContext;
    int mResource;

    TextView cp_code_text,
             cp_status_text,
             beacon_text,
             daytime_text,
             time_from_start_text,
             split_time_text;


    public SplitListAdapter(Context context, int resource, ArrayList<Split> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        cp_code_text = convertView.findViewById(R.id.cp_code_text);
        cp_status_text = convertView.findViewById(R.id.cp_status_text);
        beacon_text = convertView.findViewById(R.id.beacon_text);
        daytime_text = convertView.findViewById(R.id.daytime_text);
        time_from_start_text = convertView.findViewById(R.id.time_from_start_text);
        split_time_text = convertView.findViewById(R.id.split_time_text);

        cp_code_text.setText(String.valueOf(getItem(position).getCode()));
        daytime_text.setText(MainActivity.longTimeToString(getItem(position).getSplitTime()));
        time_from_start_text.setText(MainActivity.longTimeToString(getItem(position).getRelTime()));
        split_time_text.setText(MainActivity.longTimeToString(getItem(position).getPunchTime()));

        if(getItem(position).isBeacon()){
            beacon_text.setText("✓");
        }else{
            beacon_text.setText("X");
        }

        if(getItem(position).getCPStatus() != 0){
            cp_status_text.setText(getItem(position).getCPStatus());
        }

        return convertView;
    }


}
