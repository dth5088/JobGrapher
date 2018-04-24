/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import java.time.Duration;
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
    
    public String getTotalDuration() {
        int numSeconds = 0;
        for(Stage stage : stages.values()) {
            numSeconds += stage.getDuration().getSeconds();
        }
        int days = numSeconds / 86400;
        int hours = (numSeconds % 86400) / 3600;
        int minutes = ((numSeconds % 86400) % 3600 ) / 60;
        int seconds = ((numSeconds % 86400) % 3600 ) % 60;
        return "Job Duration: " +hours+":"+minutes+":"+seconds;
    }
    
    public double getTotalBarrels() {
        double totalBarrels = 0.0;
        for(Stage stage : stages.values())
        {
            totalBarrels += stage.getTotalBarrels();
        }
        return totalBarrels;
    }
    
    private double getAveragePressure() {
        double averagePressure = 0.0;
        for(Stage stage : stages.values())
        {
            averagePressure += stage.getAveragePressure();
        }
        return averagePressure / stages.size();
    }
    
    private double getAverageWaterRate() {
        double average = 0.0;
        for(Stage stage : stages.values())
        {
            average += stage.getAverageWaterRate();
        }
        return average / stages.size();
    }
    
    private double getAverageSandRate() {
        double average = 0.0;
        for(Stage stage : stages.values())
        {
            average += stage.getAverageSandRate();
        }
        return average / stages.size();
    }
    
    public HashMap<String,Double> getOutliers() {
        double minPressure = 0.0, maxPressure = 0.0, minWaterRate = 0.0
                , maxWaterRate = 0.0, minSandRate = 0.0, maxSandRate = 0.0;
        minPressure = 100.00;
        minWaterRate = 25.00;
        minSandRate = 5.00;
        for(Stage stage : stages.values())
        {
            if(stage.getMaxPressure() > maxPressure)
                maxPressure = stage.getMaxPressure();
            if(stage.getMinPressure() < minPressure && stage.getMinPressure() > 0)
                minPressure = stage.getMinPressure();
            if(stage.getMinWaterRate() < minWaterRate&& stage.getMinWaterRate() > 0)
                minWaterRate = stage.getMinWaterRate();
            if(stage.getMaxWaterRate() > maxWaterRate)
                maxWaterRate = stage.getMaxWaterRate();
            if(stage.getMinSandRate() < minSandRate && stage.getMinSandRate() > 0)
                minSandRate = stage.getMinSandRate();
            if(stage.getMaxSandRate() > maxSandRate)
                maxSandRate = stage.getMaxSandRate();
            
        }
        HashMap<String,Double> outliers = new HashMap<>();
        outliers.put("minPressure", minPressure);
        outliers.put("maxPressure", maxPressure);
        outliers.put("minWaterRate", minWaterRate);
        outliers.put("maxWaterRate", maxWaterRate);
        outliers.put("minSandRate", minSandRate);
        outliers.put("maxSandRate", maxSandRate);
        outliers.put("averagePressure", getAveragePressure());
        outliers.put("averageWaterRate", getAverageWaterRate());
        outliers.put("averageSandRate", getAverageSandRate());
        
        return outliers;
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
    
    public ArrayList<String> getDates() {
        ArrayList<String> dates = new ArrayList<>();
        for(Stage stage : stages.values())
        {
            for(String date: stage.dates())
            {
                if(!dates.contains(date))
                    dates.add(date);
            }
        }
        return dates;
    }
    
    
}

