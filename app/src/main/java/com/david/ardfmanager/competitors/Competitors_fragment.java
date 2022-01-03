package com.david.ardfmanager.competitors;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.R;
import com.david.ardfmanager.category.Category;

import java.util.ArrayList;

public class Competitors_fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static ListView mListView;

    static View view;

    //competitor add dialog okynko promeny vole
    static int ID, SINumber, gender, yearOfBirth, startNumber;
    static String name, surname, category, callsign, country, index;


    public Competitors_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment competitors.
     */
    // TODO: Rename and change types and number of parameters
    public static Competitors_fragment newInstance(String param1, String param2) {
        Competitors_fragment fragment = new Competitors_fragment();
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

        view = inflater.inflate(R.layout.fragment_competitors, container, false);
        mListView = (ListView) view.findViewById(R.id.competitorsListView);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(MainActivity.competitorsListAdapter);
        return view;
    }

    public static Competitor findCompBySI(long SINumber){
        ArrayList<Competitor> arrayList = MainActivity.event.getCompetitorsList();
        for(Competitor competitor : arrayList) {
            if(competitor.getSINumber() == SINumber){
                return competitor;
            }
        }
        return null;
    }

    public static void showCompetitorAddDialog(Context c, Competitor competitor) {

        ViewGroup viewGroup = view.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(c).inflate(R.layout.dialog_competitor_add, viewGroup, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.add_competitor);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //set the dialog size based on screen size
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.height = (int) (MainActivity.displayMetrics.heightPixels * 0.8f);
        alertDialog.getWindow().setAttributes(layoutParams);

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText surnameEditText = dialogView.findViewById(R.id.surnameEditText);
        EditText tagNumberNumber = dialogView.findViewById(R.id.tagNumberNumber);
        EditText countryEditText = dialogView.findViewById(R.id.countryEditText);
        EditText indexEditText = dialogView.findViewById(R.id.indexEditText);
        EditText startNumberNumber = dialogView.findViewById(R.id.startNumberNumber);
        EditText callSignEditText = dialogView.findViewById(R.id.callSignEditText);
        EditText startTimeEditText = dialogView.findViewById(R.id.startTimeEditText);

        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        NumberPicker birthYear = dialogView.findViewById(R.id.birthYear);
        birthYear.setMinValue(MainActivity.MIN_BIRTH_YEAR);
        birthYear.setMaxValue(MainActivity.MAX_BIRTH_YEAR);
        birthYear.setValue(birthYear.getMaxValue()-10);

        Switch genderSwitch = dialogView.findViewById(R.id.genderSwitch);
        Button confButt = dialogView.findViewById(R.id.confButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(c, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        ArrayList<String> spinnerStringCategories = new ArrayList<>();
        spinnerStringCategories.add(c.getResources().getString(R.string.select_category));
        for(Category cat : MainActivity.event.getCategoriesList()){
            spinnerStringCategories.add(cat.getName());
        }
        ArrayAdapter<String> spinnerCategoriesAdapter  = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, spinnerStringCategories);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerCategoriesAdapter);

        genderSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(genderSwitch.isChecked()){
                    genderSwitch.setText(R.string.female);
                }else{
                    genderSwitch.setText(R.string.male);
                }
            }
        });

        confButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.checkFilled(c, nameEditText) && MainActivity.checkFilled(c, surnameEditText)) {
                    String name = nameEditText.getText().toString();
                    String surname = surnameEditText.getText().toString();

                    int ID = 0;
                    String category = "kategorie";
                    int gender = genderSwitch.isActivated() ? 1 : 0;

                    if (!tagNumberNumber.getText().toString().equals("")) {
                        SINumber = Integer.parseInt(tagNumberNumber.getText().toString());
                    } else {
                        SINumber = -1;
                    }

                    yearOfBirth = birthYear.getValue();

                    if (!startNumberNumber.getText().toString().equals("")) {
                        startNumber = Integer.parseInt(startNumberNumber.getText().toString());
                    } else {
                        startNumber = -1;
                    }
                    callsign = callSignEditText.getText().toString();
                    country = countryEditText.getText().toString();
                    index = indexEditText.getText().toString();

                    Competitor competitor = new Competitor(ID, SINumber, name, surname, category, gender, yearOfBirth, callsign, country, startNumber, index);
                    MainActivity.event.addCompetitor(competitor);
                    MainActivity.setAllAdaptersAndSave();
                    alertDialog.dismiss();
                }
            }
        });

    }
}
