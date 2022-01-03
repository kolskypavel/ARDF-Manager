package com.david.ardfmanager.split;

public class Split {
    private int code; // the SI code number
    private char CPStatus = 0; // The status of the control + valid control, - control already taken before, ? - invalid control for the category
    private boolean beacon = false; // 0 - not a beacon, 1 - beacon
    private long punchTime; // The TOD of the punch
    private long relTime;   // The time from the start
    private long splitTime; // The split time between two Punches

    //constructors
    public Split(int code, char CPStatus, boolean beacon, long punchTime, long relTime, long splitTime) {
        this.code = code;
        this.CPStatus = CPStatus;
        this.beacon = beacon;
        this.punchTime = punchTime;
        this.relTime = relTime;
        this.splitTime = splitTime;
    }

    public Split(int code, long punchTime, long relTime, long splitTime) {
        this.code = code;
        this.punchTime = punchTime;
        this.relTime = relTime;
        this.splitTime = splitTime;
    }

    //getters and setters

    public boolean isBeacon() {
        return beacon;
    }

    public void setBeacon(boolean beacon) {
        this.beacon = beacon;
    }

    public char getCPStatus() {
        return CPStatus;
    }

    public void setCPStatus(char CPStatus) {
        this.CPStatus = CPStatus;
    }

    public boolean getType() {
        return beacon;
    }

    public void setType(boolean beacon) {
        this.beacon = beacon;
    }

    public long getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(long punchTime) {
        this.punchTime = punchTime;
    }

    public long getRelTime() {
        return relTime;
    }

    public void setRelTime(long relTime) {
        this.relTime = relTime;
    }

    public long getSplitTime() {
        return splitTime;
    }

    public void setSplitTime(long splitTime) {
        this.splitTime = splitTime;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
/*
public class Split {
    private int type; //0 - start, 1 - CP, 2 - finish
    private int CPcode; //used only if type is 1
    private long timeOnTrack; //time from start
    private long timeFromLastSplit; //time gap between splits
    private long daytime; //speaks for itself :D


    public Split(int type, int cPcode, long timeOnTrack, long timeFromLastSplit, long daytime) {
        this.type = type;
        CPcode = cPcode;
        this.timeOnTrack = timeOnTrack;
        this.timeFromLastSplit = timeFromLastSplit;
        this.daytime = daytime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCPcode() {
        return CPcode;
    }

    public void setCPcode(int CPcode) {
        this.CPcode = CPcode;
    }

    public long getTimeOnTrack() {
        return timeOnTrack;
    }

    public void setTimeOnTrack(long timeOnTrack) {
        this.timeOnTrack = timeOnTrack;
    }

    public long getTimeFromLastSplit() {
        return timeFromLastSplit;
    }

    public void setTimeFromLastSplit(long timeFromLastSplit) {
        this.timeFromLastSplit = timeFromLastSplit;
    }

    public long getDaytime() {
        return daytime;
    }

    public void setDaytime(long daytime) {
        this.daytime = daytime;
    }
}*/
