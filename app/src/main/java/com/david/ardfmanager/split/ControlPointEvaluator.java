package com.david.ardfmanager.split;

import com.david.ardfmanager.MainActivity;
import com.david.ardfmanager.controlpoint.ControlPoint;

import java.util.ArrayList;

public class ControlPointEvaluator {

    /* This class serves as an evaluation mechanism for the punches.
    It's purpose is to calculate the final Fox number and eventually filter the wrong ones.
     */

    //fields section
    //TODO connect them with actual competitors and controlPoints

    private int eventType = MainActivity.event.getType(); //this value is obtained from the Event class: 0-classics, 1-foxoring, 2-sprint, 3-long
    private String category; //TODO: Add a method to get the competitors' category


    private ArrayList<Split> competitorSplits = new ArrayList<>();
    private ArrayList<ControlPoint> validCPList = new ArrayList<>();

    private int status; //The evaluation status 0 - OK, 1-Did Not Finish, 2 - Disqualified, 3 - Exceeded limit, 4 - Not evaluated
    //This method selects the right evaluation algorithm, depending on the type of the event

    public void evaluationMethodSelector(int eventType) {
        switch (eventType) {
            case 0:
                handleClassics();
                break;
            case 1:
                handleSprint();
                break;
            case 2:
                handleOrienteering();
                break;
        }
    }

    public void handleClassics() {

        //loops through all control points
        for (int i = 0; i < competitorSplits.size(); i++) {
            for (int j = 0; j < validCPList.size(); j++) {

                //checks if the first control point has the same code as one of the valid ones
                if (competitorSplits.get(i).getCode() == validCPList.get(j).getCode()) {
                    competitorSplits.get(i).setCPStatus('+');

                } else {
                    //In this case competitor took CP which he was not supposed to take
                    competitorSplits.get(i).setCPStatus('?');
                }
            }
        }

        //this loops solves the redundancy
        for (int k = 0; k < competitorSplits.size(); k++) {
            for (int l = 0; l < validCPList.size(); l++) {

                if (competitorSplits.get(k).getCode() == competitorSplits.get(l).getCode()) {
                    competitorSplits.get(k).setCPStatus('-');
                }
            }
        }

        //this checks if beacon is last CP
        for (int m = 0; m < competitorSplits.size(); m++) {
        }

    }


    public void handleSprint() {
        /*Checks for the  */
    }

    public void handleOrienteering() {
        /*Checks if the sequence of controls is the same as the validOnes, even if the sequence is
        broken at some point */

        for (int i = 0; i < competitorSplits.size(); i++) {
            if (competitorSplits.get(i).getCode() == validCPList.get(i).getCode()) {
                competitorSplits.get(i).setCPStatus('+');
            }

        }
    }

    public static void classicsLoopHandler() {

    }


    public int foxCounter() {
        int counter = 0;


        return counter;

    }

}
