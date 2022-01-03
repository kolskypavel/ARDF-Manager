package com.david.ardfmanager.SI;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Punch implements Parcelable, Serializable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Punch createFromParcel(Parcel in) {
            return new Punch(in);
        }

        public Punch[] newArray(int size) {
            return new Punch[size];
        }
    };

    public int code; //The SI station code
    public long time; //The time of the punch

    public Punch() {
    }

    public Punch(int code, long time) {
        this.code = code;
        this.time = time;
    }

    public Punch(Parcel in) {
        this.code = in.readInt();
        this.time = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.code);
        dest.writeLong(this.time);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

  }

