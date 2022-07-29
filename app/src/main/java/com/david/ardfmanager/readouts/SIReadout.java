package com.david.ardfmanager.readouts;

import com.david.ardfmanager.readouts.SI.Punch;
import com.david.ardfmanager.readouts.split.Split;

import java.io.Serializable;
import java.util.ArrayList;

public class SIReadout implements Serializable {

    private int ID;
    private long cardId;
    private long startTime;
    private long finishTime;
    private ArrayList<Punch> punches;
    private ArrayList<Split> splits;

    public SIReadout(){}

    public SIReadout(int ID, long cardId, long startTime, long finishTime, ArrayList<Punch> punches) { //ArrayList<CardReader.CardEntry.Punch> punches
        this.ID = ID;
        this.cardId = cardId;
        this.startTime = startTime;
        this.finishTime = finishTime;
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

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}

