package com.david.ardfmanager.event;

import android.content.res.Resources;

import com.david.ardfmanager.R;
import com.david.ardfmanager.category.Category;
import com.david.ardfmanager.competitors.Competitor;
import com.david.ardfmanager.controlpoint.ControlPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Event implements Serializable {

    private String title;
    private int level;  //1-national ,2-regional ,3-district ,4-not a championship
    private int band;   //0-combinated, 1-80m, 2-2m
    private int type;   //0-classics, 1-foxoring, 2-sprint, 3-long

    //private ArrayList<Track> tracksList = new ArrayList<Track>();
    private ArrayList<ControlPoint> controlPointList = new ArrayList<ControlPoint>();
    private ArrayList<Category> categoriesList = new ArrayList<Category>();
    private ArrayList<Competitor> competitorsList = new ArrayList<Competitor>();


    public Event(String title, int level, int band, int type) {
        this.title = title;
        this.level = level;
        this.band = band;
        this.type = type;
    }

    public Event(String title, int level, int band, int type, ArrayList<Category> categoriesList, ArrayList<Competitor> competitorsList) {
        this.title = title;
        this.level = level;
        this.band = band;
        this.type = type;
        this.categoriesList = categoriesList;
        this.competitorsList = competitorsList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBand() {
        return band;
    }

    public void setBand(int band) {
        this.band = band;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCompetitorsList(ArrayList<Competitor> competitorsList) {
        this.competitorsList = competitorsList;
    }

    public ArrayList<Competitor> getCompetitorsList() {
        return competitorsList;
    }

    public void addCompetitor(Competitor competitor){
        this.competitorsList.add(competitor);
    }


    public ArrayList<Category> getCategoriesList() {
        return categoriesList;
    }
    public void setCategoriesList(ArrayList<Category> categoriesList) {
        this.categoriesList = categoriesList;
    }

    public void addCategory(Category category){
        this.categoriesList.add(category);
    }

    public void editCategory(Category oldCategory, Category newCategory){
        oldCategory.setName(newCategory.getName());
        oldCategory.setLength(newCategory.getLength());
        oldCategory.setMinAge(newCategory.getMinAge());
        oldCategory.setMaxAge(newCategory.getMaxAge());
        oldCategory.setControlPoints(newCategory.getControlPoints());
    }

    public ArrayList<ControlPoint> getControlPoints(){
        return this.controlPointList;
    }

    public void addControlPoint(ControlPoint c){
        this.controlPointList.add(c);
    }


}
