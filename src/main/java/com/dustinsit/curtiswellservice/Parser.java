/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;


/**
 *
 * @author dth5088
 */
public class Parser {
    Job job;
    JTabbedPane tabbedPane;
    JPanel stageChartPanel;
    private Crosshair xCrosshair;
    JButton deleteButton = new JButton("Delete"), printButton = new JButton("Export PDF");
    JTextField startDateField,endDateField;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    JLabel startLabel = new JLabel("Start Date:"), endLabel = new JLabel("End Date:");
    int currentStage = 0;
    JPanel deleteStageDataPanel;
    
    
    public void readFile(File file) throws FileNotFoundException {
        tabbedPane = new JTabbedPane();
        FileReader fr = new FileReader(file);
        Scanner scanner = new Scanner(fr);
        String jobDetails = scanner.nextLine();
        createJob(jobDetails);
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            process(line);
            
        }
    }
    private void setupDeleteOptions() {
       startDateField = new JTextField();
       endDateField = new JTextField();
       deleteStageDataPanel = new JPanel(new GridBagLayout());
       GridBagConstraints gbc = new GridBagConstraints();
       
       gbc.gridx = 0;
       gbc.gridy = 0;
       gbc.fill = GridBagConstraints.NONE;
       gbc.weightx = 1;
       gbc.anchor = GridBagConstraints.EAST;
       gbc.insets = new Insets(0,5,0,5);
       deleteStageDataPanel.add(startLabel, gbc);
       
       gbc.gridx = 1;
       gbc.fill = GridBagConstraints.HORIZONTAL;
       deleteStageDataPanel.add(startDateField, gbc);
       
       gbc.gridx = 2;
       gbc.fill = GridBagConstraints.NONE;
       gbc.anchor = GridBagConstraints.EAST;
       deleteStageDataPanel.add(endLabel, gbc);
       
       gbc.gridx = 3;
       gbc.fill = GridBagConstraints.HORIZONTAL;
       deleteStageDataPanel.add(endDateField, gbc);
       
       gbc.gridx = 4;
       deleteStageDataPanel.add(deleteButton, gbc);
       deleteButton.addActionListener((event) -> {
           executeDeletion();
       }); 
       
    }
    
    private void executeDeletion() {
        if(startDateField.getText().isEmpty() || endDateField.getText().isEmpty())
            return;
        try {
   
               Date startDate = formatter.parse(startDateField.getText());
               Date endDate = formatter.parse(endDateField.getText());
               Stage stage = job.getStage(currentStage);
               if(stage.deleteRecordingsBetween(startDate, endDate))
               {
                   System.out.println("Deletion Successful");
                   ChartPanel chartPanel = new ChartPanel(stage.getChart());
                   setupChartPanel(chartPanel);
                   startDateField.setText("");
                   endDateField.setText("");
               }
               else
                   System.out.println("Deletion Unsuccessful");
               
           } catch(ParseException e) {
               e.printStackTrace();
           }
    }
    
    protected synchronized void createPDF() throws Exception
    {      
        String home = System.getProperty("user.home");
        String pdfName = home+"/Downloads/"+job.getJobName() + "_" + job.getWellNumber() + ".pdf";
        System.out.println(pdfName);
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfName));
        Document doc = new Document(pdfDoc, new PageSize(PageSize.A4), false);
        PdfFont font = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
        PdfFont bold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);

        Text jobName = new Text(job.getJobName()).setFont(bold).setFontSize(24);
        Text jobWellNum = new Text("Well: " + job.getWellNumber()).setFont(bold).setFontSize(24);
        Text jobLot = new Text("Lot: " + job.getLot()).setFont(bold).setFontSize(24);
        Paragraph jName = new Paragraph().add(jobName);
        doc.showTextAligned(jName, 300, 450, TextAlignment.CENTER, VerticalAlignment.MIDDLE);
        //doc.add(jName);
        
        Paragraph wellNo = new Paragraph().add(jobWellNum);
        doc.showTextAligned(wellNo, 300, 425, TextAlignment.CENTER, VerticalAlignment.MIDDLE);
        //doc.add(wellNo);
        //doc.add(new Paragraph().add(jobName).add(jobWellNum).add(jobLot));
        Paragraph lot = new Paragraph().add(jobLot);
        doc.showTextAligned(lot, 300, 400, TextAlignment.CENTER, VerticalAlignment.MIDDLE);
        doc.add(new AreaBreak(AreaBreakType.NEXT_AREA));
        
        
        Iterator<Stage> it = job.getStages().iterator();
        while(it.hasNext())
        {
            
            Stage stage = it.next();
            JFreeChart stageChart = stage.getChart();
            
            Image img = convertChartToImage(stageChart);
            img.setAutoScaleWidth(true);
            img.setHorizontalAlignment(HorizontalAlignment.CENTER);
            doc.add(img);
 
            
        }
        
        
      
        doc.close();    
    }
    
    
    private Image convertChartToImage(JFreeChart chart) throws IOException {
        Image result = null;
        BufferedImage original = chart.createBufferedImage(600,400);
        
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(original, "png", os);
            os.flush();
            
            ImageData image = ImageDataFactory.create(os.toByteArray());
            result = new Image(image);
        }
        return result;
    }
    public JPanel getDeleteStageDataPanel() {
        return deleteStageDataPanel;
    }
    
    private void setupChartPanel() {
        stageChartPanel = new JPanel();
        Stage first = job.getStages().get(0);
        currentStage = first.getStageNumber();
        ChartPanel chartPanel = new ChartPanel(first.getChart());
        chartPanel.setPreferredSize(new Dimension(850,650));
        
        chartPanel.getPopupMenu().addSeparator();
        
        JMenuItem deleteAnnotationItem = new JMenuItem("Delete Comment(s)");
        chartPanel.getPopupMenu().add(deleteAnnotationItem);
        deleteAnnotationItem.addActionListener((ev) -> {
            int size = job.getStage(currentStage).getAnnotations().size();
            //XYTextAnnotation[] choices = new XYTextAnnotation[size];
            HashMap<String,XYTextAnnotation> map = new HashMap<>();
            String[] choices = new String[size];
            int i = 0;
            for(XYTextAnnotation ann : job.getStage(currentStage).getAnnotations())
            {
                choices[i] = ann.getText();
                map.put(ann.getText(), ann);
                i++;
            }
            String ann = (String) JOptionPane.showInputDialog(null, "Select a comment to delete", "Delete Comment", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
            
            if(job.getStage(currentStage).deleteAnnotation(map.get(ann)))   
                setupChartPanel(chartPanel);
            
        });
        
        chartPanel.getPopupMenu().addSeparator();
        JMenuItem startDateMenuItem = new JMenuItem("Set Start Date");
        chartPanel.getPopupMenu().add(startDateMenuItem);
        startDateMenuItem.addActionListener((event) -> {
            XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = 0;
            try {
                x = xAxis.java2DToValue(chartPanel.getMousePosition().getX(), chartPanel.getScreenDataArea(), RectangleEdge.BOTTOM);
                Date date = new Date((long)x);
                if(startDateField.getText().length() > 0)
                    startDateField.setText("");
                startDateField.setText(formatter.format(date));
            } catch(NullPointerException e) {
                System.out.println("No point on X-Axis.");
            }
        });
        
        
        JMenuItem endDateMenuItem = new JMenuItem("Set End Date");
        chartPanel.getPopupMenu().add(endDateMenuItem);
        endDateMenuItem.addActionListener((event) -> {
            XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = xAxis.java2DToValue(chartPanel.getMousePosition().getX(), chartPanel.getScreenDataArea(), RectangleEdge.BOTTOM);
            Date date = new Date((long)x);
            if(endDateField.getText().length() > 0)
                endDateField.setText("");
            endDateField.setText(formatter.format(date));
            
        });
        setupChartListener(chartPanel);
        stageChartPanel.add(chartPanel);
        
        
    }
    private void addNote(ChartPanel chartPanel, double x , double y) {
        JFreeChart chart = chartPanel.getChart();
        XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
        String note = (String) JOptionPane.showInputDialog(null, "Enter Comment\n", "Add Annotation", JOptionPane.PLAIN_MESSAGE);
        XYTextAnnotation annotation = new XYTextAnnotation(note, x, y);
        annotation.setPaint(Color.RED);
        plot.addAnnotation(annotation);
        job.getStage(currentStage).addAnnotationToChart(annotation);
    }
    
    private void setupChartPanel(ChartPanel chartPanel) {
        stageChartPanel.remove(0);
        chartPanel.setPreferredSize(new Dimension(850,650));
        
        chartPanel.getPopupMenu().addSeparator();
        JMenuItem deleteAnnotationItem = new JMenuItem("Delete Comment(s)");
        chartPanel.getPopupMenu().add(deleteAnnotationItem);
        deleteAnnotationItem.addActionListener((ev) -> {
            int size = job.getStage(currentStage).getAnnotations().size();
            if(size == 0)
                return;
            //XYTextAnnotation[] choices = new XYTextAnnotation[size];
            HashMap<String,XYTextAnnotation> map = new HashMap<>();
            String[] choices = new String[size];
            int i = 0;
            for(XYTextAnnotation ann : job.getStage(currentStage).getAnnotations())
            {
                choices[i] = ann.getText();
                map.put(ann.getText(), ann);
                i++;
            }
            String ann = (String) JOptionPane.showInputDialog(null, "Select a comment to delete", "Delete Comment", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
            System.out.println(ann);
            if(job.getStage(currentStage).deleteAnnotation(map.get(ann)))   
            {
                setupChartPanel(chartPanel);
            }
            
        });
        
        chartPanel.getPopupMenu().addSeparator();
        
        JMenuItem startDateMenuItem = new JMenuItem("Set Start Date");
        chartPanel.getPopupMenu().add(startDateMenuItem);
        startDateMenuItem.addActionListener((event) -> {
            XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = 0;
            try {
                x = xAxis.java2DToValue(chartPanel.getMousePosition().getX(), chartPanel.getScreenDataArea(), RectangleEdge.BOTTOM);
                Date date = new Date((long)x);
                if(startDateField.getText().length() > 0)
                    startDateField.setText("");
                startDateField.setText(formatter.format(date));
            } catch(NullPointerException e) {
                System.out.println("No point on X-Axis.");
            }
        });
        
        JMenuItem endDateMenuItem = new JMenuItem("Set End Date");
        chartPanel.getPopupMenu().add(endDateMenuItem);
        endDateMenuItem.addActionListener((event) -> {
            XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = xAxis.java2DToValue(chartPanel.getMousePosition().getX(), chartPanel.getScreenDataArea(), RectangleEdge.BOTTOM);
            Date date = new Date((long)x);
            if(endDateField.getText().length() > 0)
                endDateField.setText("");
            endDateField.setText(formatter.format(date));
            
        });
        setupChartListener(chartPanel);
        stageChartPanel.add(chartPanel);
        stageChartPanel.revalidate();
    }
    public Container setupButtonPane() {
        setupDeleteOptions();
        printButton.addActionListener((event) -> {
            try {
                createPDF();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        
        JPanel returnPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JPanel buttonPanel = setupStageButtons();
        setupChartPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        returnPanel.add(buttonPanel);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx = 1;
        returnPanel.add(stageChartPanel);
        
        gbc.insets = new Insets(10,0,10,0);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        returnPanel.add(printButton, gbc);
        return returnPanel;
    }
    
    private JPanel setupStageButtons() {
        JPanel panel = new JPanel(new GridLayout(0,2,2,2));
        for(Stage stage : job.getStages())
        {
            JButton button = job.getStageButton(stage);
            setupButtonEvent(button);
            panel.add(button);
            
        }
        
        return panel;
    }
    
    private void setupButtonEvent(JButton button) {
        button.addActionListener((e) -> {
            String bName = button.getText();
            String[] splitString = bName.split("\\s");
            int stageNumber = Integer.parseInt(splitString[1]);
            Stage stage = job.getStage(stageNumber);
            ChartPanel chartPanel = new ChartPanel(stage.getChart());
            chartPanel.setPreferredSize(new Dimension(850,650));
            setupChartListener(chartPanel);
            stageChartPanel.remove(0);
            stageChartPanel.add(chartPanel);
            stageChartPanel.revalidate();
        });
    }
    HashMap<Point2D,XYTextAnnotation> pTa = new HashMap<>();
    private void setupChartListener(ChartPanel chartPanel) {
        CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
        xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        crosshairOverlay.addDomainCrosshair(xCrosshair);
        chartPanel.addOverlay(crosshairOverlay);
        SimpleDateFormat format = new SimpleDateFormat("H:mm:ss");
        
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            
            @Override
            public void chartMouseClicked(ChartMouseEvent e) {
                       
                if(e.getTrigger().getClickCount() == 2) 
                {
                    
                    Point2D p = chartPanel.translateScreenToJava2D(e.getTrigger().getPoint());
                    Rectangle2D plotArea = chartPanel.getScreenDataArea();
                    XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
                    double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
                    double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
                    addNote(chartPanel, chartX, chartY);

                }
                
            }
           
            @Override
            public void chartMouseMoved(ChartMouseEvent e) {
                final ChartEntity entity = e.getEntity();
                Point2D p = chartPanel.translateScreenToJava2D(e.getTrigger().getPoint());
                Rectangle2D plotArea = chartPanel.getScreenDataArea();
                XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
                double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
                double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
                double y2 = DatasetUtilities.findYValue(plot.getDataset(1), 0, chartX);
                Date date = new Date((long) chartX);
                xCrosshair.setValue(chartX);
                entity.setToolTipText("Time: " + format.format(date) + "\n Pressure: " + String.format("%.2f", chartY) + "\n Rate: " + String.format("%.2f",y2));
                
            }
            
        });
    }
    
    private void createJob(String jobDetails) {
        String[] splitString = jobDetails.split(",");
        String jobName = splitString[0];
        String wellNumber = splitString[1];
        String lot = splitString[2];
        this.job = new Job(jobName,wellNumber,lot);
    }
    
    public Job getJob() {
        return job;
    }
    
    private void process(String line) {
        String[] splitString = line.split(",");
        
        String jobID = splitString[0];
        
        String stageNumber = splitString[1].replace('"', ' ');
        String pressureString = splitString[2].replace('"', ' ');
        String waterRateString = splitString[3].replace('"', ' ');
        String timeString = splitString[4].replace('"', ' ');
        String sandRateString = "";
        if(splitString.length < 6)
        {
            sandRateString = "0.00";
        }
        else if(splitString.length >= 6) {
            sandRateString = splitString[5].replace('"', ' ');
        }
         int stageNum = Integer.parseInt(stageNumber.trim());

        job.addData(stageNum, pressureString, waterRateString, timeString, sandRateString);
    }
    
    @Override
    public String toString() {
        return job.toString();
    }

    
}

