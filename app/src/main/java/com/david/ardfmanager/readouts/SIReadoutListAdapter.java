package com.david.ardfmanager.readouts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.david.ardfmanager.R;
import com.david.ardfmanager.competitors.Competitor;
import com.david.ardfmanager.competitors.Competitors_fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SIReadoutListAdapter extends ArrayAdapter<SIReadout> {

    private Context mContext;
    int mResource;

    TextView nameTextView, siNumberTextView, timeTextView, readoutTimeTextView;

    public SIReadoutListAdapter(Context context, int resource, ArrayList<SIReadout> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        long cardId = getItem(position).getCardId();
        long startTime = getItem(position).getStartTime();
        long finishTime = getItem(position).getFinishTime();
        long checkTime = getItem(position).getCheckTime();

        long timeDiff = finishTime - startTime;
        long minutes = timeDiff / (60*1000);
        long seconds = (timeDiff - minutes * 60 * 1000) / 1000;
        long hundreds = (timeDiff - minutes * 60 * 1000 - seconds * 1000);

        DecimalFormat mFormat= new DecimalFormat("00");
        String time = mFormat.format(minutes) + ":" + mFormat.format(seconds);


        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        siNumberTextView = (TextView) convertView.findViewById(R.id.siNumberTextView);
        timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
        readoutTimeTextView = (TextView) convertView.findViewById(R.id.readoutTimeTextView);



        Competitor competitor = Competitors_fragment.findCompBySI(cardId);
        if(competitor != null){
            nameTextView.setText(competitor.getFullName());
        }else{
            nameTextView.setText(R.string.not_in_database);
        }

        siNumberTextView.setText(String.valueOf(cardId));
        timeTextView.setText(time);
        //readoutTimeTextView.setText();

        return convertView;
    }


}
