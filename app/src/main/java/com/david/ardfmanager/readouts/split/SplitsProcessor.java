package com.david.ardfmanager.readouts.split;

import com.david.ardfmanager.readouts.SI.Punch;
import com.david.ardfmanager.readouts.SIReadout;

import java.util.ArrayList;

public class SplitsProcessor extends SIReadout {

    private SIReadout siReadout;

    public SplitsProcessor(SIReadout siReadout){
        this.siReadout=siReadout;
    }

    public long calculateRunTime(long startTime, long finishTime) {
        if (finishTime != 0 && finishTime > startTime) {
            return finishTime - startTime;
        } else {
            return -1;
        }
    }

    // Converts the ArrayList of punches into ArrayList of splits
   public ArrayList<Split> Convertor(ArrayList<Punch> punches ) {

        ArrayList<Split>  splits = new ArrayList<Split>();

      long startTime = super.getStartTime();
        long finishTime = super.getFinishTime();

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
        return splits;
    }

    // This method calculates the split time between two punches.
    public long calculateSplit(long punchOneTime, long punchTwoTime) {
        if (punchOneTime < 0 || punchTwoTime < 0) {
            return -1;
        }
        return punchTwoTime - punchOneTime;
    }


}
