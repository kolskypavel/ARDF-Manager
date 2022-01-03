package com.david.ardfmanager.competitors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.david.ardfmanager.R;
import com.david.ardfmanager.competitors.Competitor;

import java.util.ArrayList;

/**
 * Created by David on 17. 7. 2017.
 */

public class CompetitorsListAdapter extends ArrayAdapter<Competitor> {

    private Context mContext;
    int mResource;

    TextView nameTextView, siNumberTextView;

    public CompetitorsListAdapter(Context context, int resource, ArrayList<Competitor> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String fullName = getItem(position).getFullName();

        long siNumber = getItem(position).getSINumber();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        nameTextView = convertView.findViewById(R.id.nameTextView);
        siNumberTextView = convertView.findViewById(R.id.siNumberTextView);

        nameTextView.setText(fullName);

        if(siNumber != -1){
            siNumberTextView.setText(String.valueOf(siNumber));
        }else{
            siNumberTextView.setText("-");
        }


        return convertView;
    }
}
