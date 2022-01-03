package com.david.ardfmanager.controlpoint;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ControlPoints_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlPoints_Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static ListView mListView;
    static View view;

    public ControlPoints_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment controlPointsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ControlPoints_Fragment newInstance(String param1, String param2) {
        ControlPoints_Fragment fragment = new ControlPoints_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_control_points, container, false);
        //return inflater.inflate(R.layout.fragment_competitors, container, false);
        mListView = (ListView) view.findViewById(R.id.control_points_lv);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(MainActivity.controlPointAdapter);
        return view;
    }
}