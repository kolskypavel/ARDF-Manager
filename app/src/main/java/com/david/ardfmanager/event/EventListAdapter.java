package com.david.ardfmanager.event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.david.ardfmanager.R;

import java.util.ArrayList;

/**
 * Created by David on 17. 7. 2017.
 */

public class EventListAdapter extends ArrayAdapter<Event> {

    private Context mContext;
    int mResource;

    public EventListAdapter(Context context, int resource, ArrayList<Event> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        String[] levels = mContext.getResources().getStringArray(R.array.levels);
        String[] bands = mContext.getResources().getStringArray(R.array.bands);
        String[] types = mContext.getResources().getStringArray(R.array.types);

        TextView tvName = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvLevel = (TextView) convertView.findViewById(R.id.tvLevel);
        TextView tvBand = (TextView) convertView.findViewById(R.id.tvBand);
        TextView tvType = (TextView) convertView.findViewById(R.id.tvType);

        tvName.setText(getItem(position).getTitle());
        tvLevel.setText(levels[getItem(position).getLevel()]);
        tvBand.setText(bands[getItem(position).getBand()]);
        tvType.setText(types[getItem(position).getType()]);

        return convertView;
    }
}
