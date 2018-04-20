/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import java.awt.Color;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import static org.jfree.chart.labels.StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author dth5088
 */
public class Chart {
    String chartTitle;
    XYDataset dataset1, dataset2;
    Stage stage;
    JFreeChart chart;
    final XYItemRenderer renderer;
    final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
    XYPlot plot; 
    NumberAxis axis2;
    DateAxis axis;
    
    public Chart(String title, Stage stage) {
        chartTitle = title;
        this.stage = stage;
        createDatasets();
        chart = ChartFactory.createTimeSeriesChart(
                title,
                "Time",
                "Pressure (PSI)",
                dataset1,
                true,
                true,
                false
        );
        
        plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        axis2 = new NumberAxis("Water Rate (BBL/MIN)");
        axis2.setAutoRangeIncludesZero(false);
        axis2.setRange(0.0, 50.0);
        plot.setRangeAxis(1, axis2);
        plot.setDataset(1, dataset2);
        plot.mapDatasetToRangeAxis(1, 1);
        renderer = plot.getRenderer();
        renderer.setSeriesPaint(0,Color.BLACK);
        
        
        
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(DEFAULT_TOOL_TIP_FORMAT, DateFormat.getInstance(), NumberFormat.getInstance()) {
            @Override
            public String generateLabelString(XYDataset dataset, int series, int item) {
                DateFormat format = super.getXDateFormat();
                String d1String = super.generateLabelString(dataset,series,item);
                String d2String = super.generateLabelString(dataset2, series, item);
                String formattedD1 = d1String.replaceAll("[()]", "");
                String formattedD2 = d2String.replaceAll("[(),]", "");
                String[] splitX1 = formattedD1.split("\\s");
                String[] splitX2 = formattedD2.split("\\s");
                return String.format("%s %s %s %n %s %s %n %s %s", "Time:",splitX1[2], splitX1[3], splitX1[0], splitX1[4] + " PSI", splitX2[0], splitX2[4] + " BBL/MIN");
            }
        });
        if(renderer instanceof StandardXYItemRenderer) {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
            rr.setPlotLines(true);
        }

        
        renderer2.setSeriesPaint(0, Color.BLUE);
        renderer2.setPlotLines(true);
        
        plot.setRenderer(1, renderer2);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setRangeCrosshairVisible(true);
        plot.getRangeAxis().setRange(0.0,5000.00);
        plot.setRangeCrosshairLockedOnData(false);
        axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); 
        axis.setAutoTickUnitSelection(true);
        axis.setAutoRange(true);
         
        if(stage.hasAnnotations())
        {
            for(XYAnnotation annotation : stage.getAnnotations())
            {
                plot.addAnnotation(annotation);
            }
        }
    }
    
    public JFreeChart getChart() {
        return chart;
    }
    
    public void addAnnotation(XYAnnotation annotation) {
        plot.addAnnotation(annotation);
    }
    
    
    private void createDatasets() {
        TimeSeries s1 = new TimeSeries("Pressure");
        TimeSeries s2 = new TimeSeries("Rate");
        
        for(Recording r : stage.getStageData())
        {
            Date time = r.getTime();
            Double pressure = r.getPressure();
            Double rate = r.getWaterRate();
            s1.add(new Millisecond(time), pressure);
            s2.add(new Millisecond(time), rate);
        }
        
        final TimeSeriesCollection dataseta = new TimeSeriesCollection();
        dataseta.addSeries(s1);
        
        final TimeSeriesCollection datasetb = new TimeSeriesCollection();
        datasetb.addSeries(s2);
        
        dataset1 = dataseta;
        dataset2 = datasetb;
    }

    synchronized void removeAnnotation(XYAnnotation annotation) {
        plot.removeAnnotation(annotation);
    }
    
}
