package com.david.ardfmanager.category;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.david.ardfmanager.DecimalDigitsInputFilter;
import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.R;
import com.david.ardfmanager.controlpoint.ControlPoint;
import com.david.ardfmanager.controlpoint.ControlPointAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Categories_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Categories_fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static ListView mListView;

    static View view;

    //track add dialog ui
    static Button confButton;
    static EditText nameEditText, lengthEditText;
    static NumberPicker minAgeNumPick, maxAgeNumPick;
    static ListView cplv;
    private static ArrayList<ControlPoint> controlPointsList;
    private static ControlPointAdapter controlPointAdapter;


    public Categories_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment tracks.
     */
    // TODO: Rename and change types and number of parameters
    public static Categories_fragment newInstance(String param1, String param2) {
        Categories_fragment fragment = new Categories_fragment();
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
        view = inflater.inflate(R.layout.fragment_categories, container, false);
        //return inflater.inflate(R.layout.fragment_tracks, container, false);
        mListView = (ListView) view.findViewById(R.id.tracksListView);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        Categories_fragment.mListView.setAdapter(MainActivity.categoryListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showCategoryAddDialog(getContext(), MainActivity.event.getCategoriesList().get(i));
            }
        });

        return view;
    }

    public static void showCategoryAddDialog(Context c, Category category){
        ViewGroup viewGroup = view.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(c).inflate(R.layout.dialog_category_add, viewGroup, false);    //set layout to view

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        if(category != null){
            builder.setTitle(R.string.edit_category);
        }else {
            builder.setTitle(R.string.add_category);
        }
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.show(); //build and show the dialog

        //set the dialog size based on screen size
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.height = (int) (MainActivity.displayMetrics.heightPixels * 0.8f);
        alertDialog.getWindow().setAttributes(layoutParams);

        //assign ids to ui elements
        confButton = (Button) dialogView.findViewById(R.id.confButton);
        nameEditText = (EditText) dialogView.findViewById(R.id.nameEditText);
        lengthEditText = (EditText) dialogView.findViewById(R.id.lengthEditText);
        lengthEditText.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2)});
        minAgeNumPick = dialogView.findViewById(R.id.birthYear);
        maxAgeNumPick = dialogView.findViewById(R.id.maxAgeNumPick);

        cplv = (ListView) dialogView.findViewById(R.id.control_points_lv);
        controlPointsList = new ArrayList<ControlPoint>();
        controlPointAdapter = new ControlPointAdapter(c, R.layout.controlpoint_view_layout,
                //controlPointsList
                MainActivity.event.getControlPoints());

        cplv.setAdapter(controlPointAdapter);

        ListAdapter listadp = cplv.getAdapter();
        if (listadp != null) {
            int totalHeight = 0;
            for (int i = 0; i < listadp.getCount(); i++) {
                View listItem = listadp.getView(i, null, cplv);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = cplv.getLayoutParams();
            params.height = totalHeight + (cplv.getDividerHeight() * (listadp.getCount() - 1));
            cplv.setLayoutParams(params);
            cplv.requestLayout();
        }

        /*cplv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                for(int a = 0; a < adapterView.getChildCount(); a++)
                {
                    adapterView.getChildAt(a).setBackgroundColor(Color.TRANSPARENT);
                }

                view.setBackgroundColor(Color.GREEN);
            }
        });*/

        minAgeNumPick.setMinValue(MainActivity.MIN_BIRTH_YEAR);
        minAgeNumPick.setMaxValue(MainActivity.MAX_BIRTH_YEAR);

        maxAgeNumPick.setMinValue(MainActivity.MIN_BIRTH_YEAR);
        maxAgeNumPick.setMaxValue(MainActivity.MAX_BIRTH_YEAR);

        minAgeNumPick.setValue(minAgeNumPick.getMaxValue()-10);
        maxAgeNumPick.setValue(maxAgeNumPick.getMaxValue()-10);

        maxAgeNumPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                minAgeNumPick.setMaxValue(maxAgeNumPick.getValue()-1);
            }
        });

        if(category != null){ //if valid track is in function arguments
            nameEditText.setText(category.getName());
            minAgeNumPick.setValue(category.getMinAge());
            maxAgeNumPick.setValue(category.getMaxAge());
            lengthEditText.setText(String.valueOf(category.getLength()));
            for(ControlPoint cp : category.getControlPoints()){
                controlPointsList.add(cp);
            }
            sortAndSet();
        }

        confButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.checkFilled(c, nameEditText) && MainActivity.checkFilled(c, lengthEditText)) {
                        Category newCategory = new Category(
                                nameEditText.getText().toString(),
                                minAgeNumPick.getValue(),
                                maxAgeNumPick.getValue(),
                                Float.parseFloat(lengthEditText.getText().toString()),
                                controlPointsList);

                        if (category == null) { //if the argument is not null, it was called from edit dialog and category is edited, else added
                            MainActivity.event.addCategory(newCategory);
                        } else {
                            MainActivity.event.editCategory(MainActivity.event.getCategoriesList().get(MainActivity.event.getCategoriesList().indexOf(category)), newCategory);
                        }

                        MainActivity.setAllAdaptersAndSave();
                        alertDialog.dismiss();
                }
            }
        });
    }

    public static void sortAndSet(){
        Collections.sort(controlPointsList, new Comparator<ControlPoint>() {
            public int compare(ControlPoint cp1, ControlPoint cp2) {
                return Integer.compare(Integer.parseInt(cp1.getName()),Integer.parseInt(cp2.getName()));
            }
        });
        cplv.setAdapter(controlPointAdapter);
    }

}
