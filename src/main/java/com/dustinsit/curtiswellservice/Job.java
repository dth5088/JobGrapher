/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JButton;

/**
 *  
 * 
 */
public class Job {
    private String jobName;
    private String wellNumber;
    private String lot;
    private String jobID ="";
    

    private HashMap<Integer,Stage> stages;
    private HashMap<Stage,JButton> stageButtons;
    private int numStages = 0;
    
    public Job(String jobName, String wellNumber, String lot) {//, String date) {
        this.jobName = jobName.replace('"', ' ').trim();
        this.wellNumber = wellNumber.replace('"', ' ').trim();
        this.lot = lot.replace('"', ' ').trim();

        stages = new HashMap<>();
        stageButtons = new HashMap<>();
    }
    
    public void setJobID(String jobID) {
        this.jobID = jobID;
    }
    
    public String getJobID() {
        return jobID;
    }
    public Integer[] getStageNumbers() {
        Integer[] stageNames = new Integer[numStages];
        int i = 0;
        SortedSet<Integer> values = new TreeSet<>(stages.keySet());
        for(Integer each : values)
        {
            stageNames[i] = each;
            i++;
        }
        return stageNames;
    }
    
    public void addStage(Stage stage) {
        stages.put(stage.getStageNumber(), stage);
        JButton tempButton = new JButton("Stage " + stage.getSubStage());
        tempButton.setName(stage.getStageNumber() + "");
        stageButtons.put(stage, tempButton);
        numStages++;
    }
    
    public void addData(int stageNumber, String pressure, String waterRate, String time, String sandRate) {
        if(stages.get(stageNumber) == null)
        {
            Stage tempStage = new Stage(stageNumber);
            stages.put(stageNumber, tempStage);
            stageButtons.put(tempStage, new JButton("Stage " + stageNumber));
            numStages++;
        }
        stages.get(stageNumber).addRecording(pressure,waterRate,time,sandRate);
    }
    
    public String getJobName() {
        return jobName;
    }
    
    public String getWellNumber() {
        return wellNumber;
    }
    
    public String getLot() {
        return lot;
    }
    public JButton getStageButton(int stageNumber) {
        Stage stage = stages.get(stageNumber);
        return stageButtons.get(stage);
    }
    
    public JButton getStageButton(Stage stage) {
        return stageButtons.get(stage);
    }
    public int getNumStages() {
        return numStages;
    }
    public ArrayList<Stage> getStages() {
        ArrayList<Stage> stageData = new ArrayList<>();
        for(Stage stage : stages.values())
        {
            stageData.add(stage);
        }
        stageData.sort(Comparator.comparing(Stage::getStageNumber));
        return stageData;
    }
    public Stage getStage(int stageNum) {
        return stages.get(stageNum);
            
    }
    public String toString() {
        String str = "";
        str += String.format("%s \t%s \t%s \n", jobName, wellNumber, lot);
        for(Stage stage : stages.values())
        {
            str += stage;
        }
        return str;
    }
    
    
}

