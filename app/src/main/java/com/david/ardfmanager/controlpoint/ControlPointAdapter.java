package com.david.ardfmanager.controlpoint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.david.ardfmanager.R;

import java.util.ArrayList;

public class ControlPointAdapter extends ArrayAdapter<ControlPoint> {

    private Context mContext;
    int mResource;
    TextView textNumber, textCode, textType;

    public ControlPointAdapter(Context context, int resource, ArrayList<ControlPoint> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String number = getItem(position).getName();
        int code = getItem(position).getCode();
        int type = getItem(position).getType();


        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        textNumber = (TextView) convertView.findViewById(R.id.textNumber);
        textCode = (TextView) convertView.findViewById(R.id.textCode);
        textType  = (TextView) convertView.findViewById(R.id.textType);


        textNumber.setText(String.valueOf(number));
        textCode.setText(String.valueOf(code));

        switch (type){
            case 0:
                textType.setText(R.string.control_point);
                break;

            case 1:
                textType.setText(R.string.beacon);
                break;

            case 2:
                textType.setText(R.string.spectator);
                break;

            case -1:
                textType.setText("Vole chyba!");
        }

        return convertView;
    }
}
