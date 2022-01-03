package com.david.ardfmanager.competitors;

import com.david.ardfmanager.split.Split;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;

public class Competitor implements Serializable {

    private int ID;
    private long SINumber;
    private String name;
    private String surname;
    private String category;
    private int gender; //0 - man, 1 - woman
    private int yearOfBirth;
    private String callsign;
    private String country;
    private int startNumber;
    private String index;

    //The times are get from the SI readout
    private long startTime;
    private long finishTime;

    //The arrayList of splits of each competitor - gonna be assigned by the readout component
    private ArrayList<Split> splits = new ArrayList<Split>();

    public Competitor(String name) {
        this.name = name;
    }

    public Competitor(int ID, long SINumber, String name, String surname, String category, int gender, int yearOfBirth, String callsign, String country, int startNumber, String index) {
        this.ID = ID;
        this.SINumber = SINumber;
        this.name = name;
        this.surname = surname;
        this.category = category;
        this.gender = gender;
        this.yearOfBirth = yearOfBirth;
        this.callsign = callsign;
        this.country = country;
        this.startNumber = startNumber;
        this.index = index;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }

    public long getSINumber() {
        return SINumber;
    }
    public void setSINumber(int tagNumber) {
        this.SINumber = tagNumber;
    }

    public String getIndex() {
        return index;
    }
    public void setIndex(String index) {
        this.index = index;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }
    public void setYear(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public int getGender() {
        return gender;
    }
    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
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

    public String getCallsign() {
        return callsign;
    }
    public void setCallsign(String Callsign) {
        this.callsign = callsign;
    }

    public String getFullName(){
        return name + " " + surname;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country= country;
    }
    public int getStartNumber() {
        return startNumber;
    }
    public void setStartNumber(int startNumber) {
        this.startNumber= startNumber;
    }

    public JSONObject competitorToJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", this.name);
        json.put("surname", this.surname);
        json.put("yearofbirth", this.yearOfBirth);
        json.put("sinumber", this.SINumber);
        json.put("gender", this.gender);
        return json;
    }
}
