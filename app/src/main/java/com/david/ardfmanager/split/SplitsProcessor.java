package com.david.ardfmanager.split;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.SI.Punch;
import com.david.ardfmanager.competitors.Competitor;
import com.david.ardfmanager.readouts.SIReadout;

import java.sql.Time;
import java.util.ArrayList;

public class SplitsProcessor {


    //TODO: Reference to the actual arrayList
    private ArrayList<Split> splits = new ArrayList<Split>();

    private long startTime;
    private long finishTime;

    public long setStartTime() {
        //This method either gets the time from the SI or sets the time based on the Competitor preset

        return 0;
    }

    public long setFinishTime() {

        return 0;
    }
    //THE IMPORTANT SHIT

    public ArrayList<Split> readoutToSplits(SIReadout siReadout) {
        startTime = siReadout.getStartTime();
        finishTime = siReadout.getFinishTime();

        splitConventor(siReadout.getPunches());

        return splits;
    }


    public long calculateRunTime(long startTime, long finishTime) {
        if (finishTime != 0 && finishTime > startTime) {
            return finishTime - startTime;
        } else {
            return -1;
        }
    }

    public void splitConventor(ArrayList<Punch> punches) {

        long relTimeCounter = 0;

        //The following loop goes through the array of punches and converts them to Splits
        for (int i = 0; i < punches.size(); i++) {
            Punch p = punches.get(i);
            long punchOneTime; //The time of the first Punch

            //In case of index 0, punchOneTime is the start Time
            if (i != 0) {
                punchOneTime = punches.get(i - 1).time;

            } else {
                punchOneTime = startTime;
            }
            long split = calculateSplit(punchOneTime, p.time); // Gets the split value
            relTimeCounter = relTimeCounter + split; //Adds the split Time to the relTime
            Split s = new Split(p.code, p.time, relTimeCounter, split); // creates and adds the new split to the Array list
            splits.add(s);
        }
    }

    // This method calculates the split time between two punches.
    public long calculateSplit(long punchOneTime, long punchTwoTime) {
        return punchTwoTime - punchOneTime;
    }


}
