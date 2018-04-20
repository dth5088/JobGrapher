/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;


/**
 *
 * @author dth5088
 */
public class Stage {
    private int stageNum;
    private Date startTime;
    private Date endTime;
    private ArrayList<Recording> stageData;
    private final String pattern = "yyyy-MM-dd HH:mm:ss";
    private final SimpleDateFormat format = new SimpleDateFormat(pattern);
    private double maxPressure = 0;
    private double minPressure = 0;
    private double maxWaterRate = 0;
    private double minWaterRate = 0;
    private double maxSandRate = 0;
    private double minSandRate = 0;
    private JFreeChart chart;
    private Chart ch;
    private ArrayList<XYAnnotation> annotations;
    private String subStage = "";
    
    
    public Stage(int stageNum) {
        this.stageNum = stageNum;
        this.stageData = new ArrayList<>();
        annotations = new ArrayList<>();
    }
    
    public Stage(int stageNum, String subStage, ArrayList<Recording> stageData) {
        this.stageNum = stageNum;
        this.stageData = new ArrayList<>(stageData);
        annotations = new ArrayList<>();
        this.subStage += subStage;
        
        
    }
    
    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }
    
    public String getSubStage() {
        return subStage;
    }
    public ArrayList<XYAnnotation> getAnnotations() {
        return annotations;
    }
    public boolean deleteAnnotation(XYAnnotation annotation) {
        boolean bool = false;
        synchronized(this)
        {
            if(annotations.contains(annotation))
            {
                annotations.remove(annotation);
                ch.removeAnnotation(annotation);
                ch = new Chart("Stage " + stageNum, this);
                bool = true;
            }
        }
     
        return bool;
    }
    
    public JFreeChart getChart() {
        if(subStage.length() > 0)
            ch = new Chart("Stage " + subStage, this);
        else
            ch = new Chart("Stage " + stageNum, this);
        
        return ch.getChart();
    }
    
    
    public boolean deleteRecordingsBetween(Date start, Date end) {
        boolean temp = false;
        ArrayList<Recording> tempRecs = new ArrayList<>(stageData);
        for(Recording recording : tempRecs)
        {
            Date current = recording.getTime();
            if(current.after(start) && current.before(end))
            {
                //stageData.get(tempRecs.indexOf(recording));
                stageData.remove(recording);
                //recording.clearValues();
                temp = true;
            }
        }
        return temp;
    }
    
    public boolean deleteAllRecordingsBefore(Date date) {
        boolean temp = false;
        ArrayList<Recording> tempRecs = new ArrayList<>(stageData);
        for(Recording record : tempRecs) {
            Date current = record.getTime();
            if(current.before(date))
            {
                stageData.remove(record);
                temp = true;
            }
        }
        return temp;
    }
    
    public boolean deleteAllRecordingsAfter(Date date) {
        boolean temp = false;
        ArrayList<Recording> tempRecs = new ArrayList<>(stageData);
        for(Recording record : tempRecs) {
            Date current = record.getTime();
            if(current.after(date))
            {
                stageData.remove(record);
                temp = true;
            }
        }
        return temp;
    }
        
    public void addRecording(String pressure, String waterRate, String time, String sandRate) {
        try {
            Date parsedTime = format.parse(time);
            Double pressureDouble = Double.parseDouble(pressure);
            Double waterDouble = Double.parseDouble(waterRate);
            Double sandDouble = Double.parseDouble(sandRate);
            Recording recording = new Recording(pressureDouble,waterDouble,parsedTime,sandDouble);
            stageData.add(recording);
            if(pressureDouble > maxPressure)
                maxPressure = pressureDouble;
            else if (pressureDouble < minPressure)
                minPressure = pressureDouble;
            if(waterDouble > maxWaterRate)
                maxWaterRate = waterDouble;
            else if (waterDouble < minWaterRate)
                minWaterRate = waterDouble;
            if(sandDouble > maxSandRate)
                maxSandRate = sandDouble;
            else if (sandDouble < minSandRate)
                minSandRate = sandDouble;
            
            if(startTime == null || startTime.after(parsedTime))
            {
                startTime = parsedTime;
            }
            else if (endTime == null || endTime.before(parsedTime))
            {
                endTime = parsedTime;
            }
        } catch(ParseException e) {
            System.out.println(e);
        }
    }
    
    public void addAnnotationToChart(XYAnnotation annotation) {
       if(ch != null)
           ch.addAnnotation(annotation);
       annotations.add(annotation);
    }
    
    public double getMaxPressure() {
        return maxPressure;
    }
    
    public double getMinPressure() {
        return minPressure;
    }
    
    public double getMaxWaterRate() {
        return maxWaterRate;
    }
    
    public double getMinWaterRate() {
        return minWaterRate;
    }
    
    public double getMaxSandRate() {
        return maxSandRate;
    }
    
    public double getMinSandRate() {
        return minSandRate;
    }
    
    public ArrayList<Recording> getStageData() {
        return this.stageData;
    }
    
    
    
    public int getStageNumber() {
        return this.stageNum;
    }
    
    
    public ArrayList<Recording> getRecordingsBetween(Date start, Date end) {
        ArrayList<Recording> temp = new ArrayList<>();
        for(Recording record : stageData)
        {
            Date current = record.getTime();
            if(current.after(start) && current.before(end))
            {
                temp.add(record);
                    
            }
        }
        removeRecordings(temp);
        return temp;
    }
    
    private boolean removeRecordings(ArrayList<Recording> records) {
        return stageData.removeAll(records);
        
    }
    
    public String toString() {
        String str = "";
        str += String.format("%10s %d %20s %tT %20s %tT %n","Stage:",stageNum,"Start Time:",startTime,"End Time:",endTime);
        for(Recording r : stageData)
        {
            str += String.format("%-30s", r);
        }
        return str;
    }
    
}

