/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import static com.itextpdf.kernel.pdf.PdfName.Annotation;
import java.awt.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.XYPlot;


/**
 *
 * @author dth5088
 */
public class Stage {
    private int stageNum;
    private Date startTime;
    private Date endTime;
    private ArrayList<Recording> stageData;
    private HashMap<Date, Recording> mappedData;
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
    private ArrayList<XYTextAnnotation> annotations;
    
    
    public Stage(int stageNum) {
        this.stageNum = stageNum;
        this.stageData = new ArrayList<>();
        mappedData = new HashMap<>();
        annotations = new ArrayList<>();
    }
    
    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }
    
    public ArrayList<XYTextAnnotation> getAnnotations() {
        return annotations;
    }
    public boolean deleteAnnotation(XYTextAnnotation annotation) {
        boolean bool = false;
        if(annotations.contains(annotation))
        {
            annotations.remove(annotation);
            ch.removeAnnotation(annotation);
            bool = true;
        }
        return bool;
    }
    
    public JFreeChart getChart() {
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
    
    public boolean deleteRecordingsAndAddGapsBetween(Date start, Date end) {
        boolean temp = false;
//        for(Recording recording : stageData)
//        {
//            if(recording.getTime().after(start) && recording.getTime().before(end))
//            {
//                recording.clearValues();
//                //stageData.remove(recording);
//                temp = true;
//            }
//        }
        
        Recording starting = null;
        Recording ending = null;
        if(mappedData.containsKey(start))
        {
            starting = mappedData.get(start);
            System.out.println("has " + start);
        }
        if(mappedData.containsKey(end))
        {
            ending = mappedData.get(end);
            System.out.println("has " + end);
        }
        
        if(starting != null && ending != null)
        {
            temp = true;
            int startingIndex = stageData.indexOf(starting);
            int endingIndex = stageData.indexOf(ending);
            
            for(int i = startingIndex; i <= endingIndex; i++)
            {
                stageData.get(i).clearValues();
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
            mappedData.put(recording.getTime(), recording);
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
    
    public void addAnnotationToChart(XYTextAnnotation annotation) {
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

