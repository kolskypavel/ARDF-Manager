package com.david.ardfmanager.category;

import com.david.ardfmanager.controlpoint.ControlPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Category implements Serializable {

    //ToDo: not show delka, pocet zavodniku

    private String name;
    private int minAge, maxAge;
    private float length;
    private ArrayList<ControlPoint> controlPoints = new ArrayList<ControlPoint>(); //stores the right CPs for the category

    public Category(String name, int minAge, int maxAge, float length, ArrayList<ControlPoint> cps){
        this.name = name;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.length = length;
        this.controlPoints = cps;
    }

    public JSONObject categoryToJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", this.name);
        json.put("minAge", this.minAge);
        json.put("maxAge", this.maxAge);
        json.put("length", this.length);
        if(!controlPoints.isEmpty()) {
            JSONArray jsonControlPointsArray = new JSONArray();
            for (int i = 0; i < controlPoints.size(); i++) {
                jsonControlPointsArray.put(controlPoints.get(i).ControlPointToJSON());
            }
            json.put("controlPoints", jsonControlPointsArray);
        }
        return json;
    }

    public int getControlPointCount() {
        return controlPoints.size();
    }

    public ArrayList<ControlPoint> getControlPoints(){
        return this.controlPoints;
    }

    public void setControlPoints(ArrayList<ControlPoint> in){
        this.controlPoints = in;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
