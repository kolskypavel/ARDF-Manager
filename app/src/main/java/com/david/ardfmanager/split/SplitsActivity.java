package com.david.ardfmanager.split;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.david.ardfmanager.R;
import com.david.ardfmanager.competitors.Competitor;
import com.david.ardfmanager.competitors.Competitors_fragment;
import com.david.ardfmanager.readouts.SIReadout;

import java.util.ArrayList;

public class SplitsActivity extends AppCompatActivity {

    Intent intent;
    Toolbar toolbar;
    TextView name_text, number_text;
    ListView splits_list_view;

    SplitsProcessor splitsProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splits);

        intent = getIntent();
        SIReadout siReadout = (SIReadout)intent.getSerializableExtra("readout");


        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.splits);
        setSupportActionBar(toolbar);

        splitsProcessor = new SplitsProcessor();

        name_text = findViewById(R.id.text_name);
        number_text = findViewById(R.id.text_number);
        splits_list_view = findViewById(R.id.splits_list_view);

        Competitor competitor = Competitors_fragment.findCompBySI(siReadout.getCardId());
        if(competitor != null){
            name_text.setText(competitor.getFullName());
        }else{
            name_text.setText(R.string.not_in_database);
        }

        number_text.setText(String.valueOf(siReadout.getCardId()));

        ////////////////////

        ArrayList<Split> splits = splitsProcessor.readoutToSplits(siReadout);
        if(splits.isEmpty()){
            Toast.makeText(SplitsActivity.this, "splits returned empty!", Toast.LENGTH_SHORT).show();
        }
        SplitListAdapter splitListAdapter = new SplitListAdapter(this, R.layout.split_view_layout, splits);
        splits_list_view.setAdapter(splitListAdapter);



    }
}


/*long t1, t2, t3;

        t1 = siReadout.getStartTime();
        t2 = siReadout.getFinishTime();
        t3 = siReadout.getCheckTime();

        ArrayList<Split> neco = new ArrayList<>();
        Split s = new Split(1, 1000, 2000, 3000);
        Split s1 = new Split(1, t1, t2, t3);
        Split s2 = new Split(1, 1000, 2000, 3000);

        neco.add(s);
        neco.add(s1);
        neco.add(s1);
        neco.add(s1);
        neco.add(s1);
        neco.add(s2);*/