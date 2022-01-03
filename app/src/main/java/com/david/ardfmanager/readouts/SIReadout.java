package com.david.ardfmanager.readouts;

import android.os.Parcel;
import android.os.Parcelable;

import com.david.ardfmanager.SI.Punch;
import com.david.ardfmanager.split.Split;

import java.io.Serializable;
import java.util.ArrayList;

public class SIReadout implements Serializable {

    private long cardId;
    private long startTime ;
    private long finishTime;
    private long checkTime;
    private ArrayList<Punch> punches;
    private ArrayList<Split> splits;


    public SIReadout(long cardId, long startTime, long finishTime, long checkTime, ArrayList<Punch> punches) { //ArrayList<CardReader.CardEntry.Punch> punches
        this.cardId = cardId;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.checkTime = checkTime;
        this.punches = punches;
    }


    public long getCardId() {
        return cardId;
    }

    public void setCardId(long cardId) {
        this.cardId = cardId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public ArrayList<Punch> getPunches() {
        return punches;
    }

    public void setPunches(ArrayList<Punch> punches) {
        this.punches = punches;
    }

    public ArrayList<Split> getSplits() {
        return splits;
    }

    public void setSplits(ArrayList<Split> splits) {
        this.splits = splits;
    }
}

