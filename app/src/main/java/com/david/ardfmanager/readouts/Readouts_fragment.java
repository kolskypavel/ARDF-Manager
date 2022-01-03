package com.david.ardfmanager.readouts;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.R;
import com.david.ardfmanager.split.SplitsActivity;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Readouts_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Readouts_fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static ListView mListView;

    public Readouts_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment readouts.
     */
    // TODO: Rename and change types and number of parameters
    public static Readouts_fragment newInstance(String param1, String param2) {
        Readouts_fragment fragment = new Readouts_fragment();
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
        View view = inflater.inflate(R.layout.fragment_readouts, container, false);

        mListView = (ListView) view.findViewById(R.id.readoutsListView);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(MainActivity.siReadoutListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //ToDO: details activity
                Intent intent = new Intent(getActivity(), SplitsActivity.class);
                intent.putExtra("readout", MainActivity.siReadoutList.get(i));
                startActivity(intent);
            }
        });
        return view;
    }
}
