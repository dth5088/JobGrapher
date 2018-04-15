/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author dth5088
 */

public class Recording {
    private String pattern = "yyyy-MM-dd HH:mm:ss";
    private SimpleDateFormat format = new SimpleDateFormat(pattern);
    private double pressureCapture, waterRateCapture, sandRateCapture;
    private Date timeCapture;
    private String stringTime;
    
    public Recording(double pressureCapture, double waterRateCapture, String stringTime, double sandRateCapture)
    {
        this.pressureCapture = pressureCapture;
        this.waterRateCapture = waterRateCapture;
        this.sandRateCapture = sandRateCapture;
        try {
            this.timeCapture = format.parse(stringTime);
        } catch(ParseException e) {}
    }
    
    public Recording(double pressureCapture, double waterRateCapture, Date timeCapture, double sandRateCapture) {
        this.pressureCapture = pressureCapture;
        this.waterRateCapture = waterRateCapture;
        this.sandRateCapture = sandRateCapture;
        this.timeCapture = timeCapture;
    }
    
    public void clearValues() {
        this.pressureCapture = 0.00;
        this.waterRateCapture = 0.00;   
    }
    
    public double getPressure() {
        return pressureCapture;
    }
    
    public double getWaterRate() {
        return waterRateCapture;
    }
    
    public double getSandRate() {
        return sandRateCapture;
    }
    
    public Date getTime() {
        return timeCapture;
    }
    
    public String toString() {
        String str = "";
        str += String.format("%20s %tT %20s %5.2f %20s %5.2f %20s %5.2f %n","Time:",timeCapture,"Pressure:",pressureCapture,"Water Rate:",waterRateCapture,"Sand Rate:",sandRateCapture);
        return str;
    }
}
