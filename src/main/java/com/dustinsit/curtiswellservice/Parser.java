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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
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
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
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
    JPanel stageChartPanel, stageButtonsPanel;
    private Crosshair xCrosshair;
    JButton overWriteButton = new JButton("Overwrite CSV"), printPortraitButton = new JButton("Save PDF (Portrait)"), printLandscapeButton = new JButton("Save PDF (Landscape)");
    
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    int currentStage = 0;
    JPanel deleteStageDataPanel;
    String fileName = "";
    Font annotationFont = new Font("TimesRoman", Font.BOLD, 18);
    final static float dash1[] = {10.0f};
    final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                                                            BasicStroke.JOIN_MITER,
                                                            10.0f, dash1, 0.0f);
    
    public void readFile(File file) throws FileNotFoundException {
        tabbedPane = new JTabbedPane();
        fileName = file.getPath();
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
    
    private boolean clearFileData() {
        boolean temp = false;
        if(fileName.length() > 0)
        {
            try {
                PrintWriter writer = new PrintWriter(fileName);
                writer.print("");
                writer.close();
                temp = true;
            } catch (FileNotFoundException ex) {
                
            }
        }
        return temp;
    }
    
    public boolean saveChangesToCSV() {
        boolean temp = false;
        if(clearFileData())
        {
            try {
                FileWriter writer = new FileWriter(fileName, true);
                BufferedWriter bw = new BufferedWriter(writer);
                String jobName = job.getJobName();
                String wellNumber = job.getWellNumber();
                String lot = job.getLot();
                String firstLine = '"' + job.getJobName() + '"' + ','+ '"' +job.getWellNumber()+'"'+','+'"'+job.getLot() + '"';   
                bw.write(firstLine);
                for(Stage stage : job.getStages())
                {
                    for(Recording record : stage.getStageData())
                    {
                        bw.newLine();
                        char comma = ',';
                        char quote = '"';
                        StringBuilder sb = new StringBuilder();
                        sb.append(job.getJobID());
                        sb.append(comma).append(quote);
                        sb.append(stage.getStageNumber());
                        sb.append(quote).append(comma).append(quote);
                        sb.append(record.getPressure());
                        sb.append(quote).append(comma).append(quote);
                        sb.append(record.getWaterRate());
                        sb.append(quote).append(comma).append(quote);
                        sb.append(formatter.format(record.getTime()));
                        sb.append(quote).append(comma).append(quote);
                        sb.append(record.getSandRate()).append(quote);
                        bw.write(sb.toString());
                    }
                }
                temp = true;
                bw.close();
            }
            catch(IOException e) {
                
            }
        }
        return temp;
    }
    
    protected synchronized String createPortraitPDF() throws Exception
    {    
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save as...");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setSelectedFile(new File(job.getJobName() + ".pdf"));
        int res = fc.showSaveDialog(null);
        String pdfName = "";
        if(res == JFileChooser.APPROVE_OPTION) {
            pdfName += fc.getSelectedFile().getAbsolutePath() + ".pdf";
        }else
        {
            JOptionPane.showMessageDialog(null,"PDF not created!");
            return null;
        }
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
        return pdfName;
    }
    
    protected synchronized String createLandscapePDF() throws Exception
    {      
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save as...");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setSelectedFile(new File(job.getJobName() + ".pdf"));
        int res = fc.showSaveDialog(null);
        String pdfName = "";
        if(res == JFileChooser.APPROVE_OPTION) {
            pdfName += fc.getSelectedFile().getAbsolutePath() + ".pdf";
        }else
        {
            JOptionPane.showMessageDialog(null,"PDF not created!");
            return null;
        }
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfName));
        Document doc = new Document(pdfDoc, new PageSize(PageSize.A4.rotate()), false);
        PdfFont font = PdfFontFactory.createFont(FontConstants.TIMES_ROMAN);
        PdfFont bold = PdfFontFactory.createFont(FontConstants.TIMES_BOLD);

        Text jobName = new Text(job.getJobName()).setFont(bold).setFontSize(24);
        Text jobWellNum = new Text("Well: " + job.getWellNumber()).setFont(bold).setFontSize(24);
        Text jobLot = new Text("Lot: " + job.getLot()).setFont(bold).setFontSize(24);
        Paragraph jName = new Paragraph().add(jobName);
        doc.showTextAligned(jName, 400, 290, TextAlignment.CENTER, VerticalAlignment.MIDDLE);
        //doc.add(jName);
        
        Paragraph wellNo = new Paragraph().add(jobWellNum);
        doc.showTextAligned(wellNo, 400, 270, TextAlignment.CENTER, VerticalAlignment.MIDDLE);
        //doc.add(wellNo);
        //doc.add(new Paragraph().add(jobName).add(jobWellNum).add(jobLot));
        Paragraph lot = new Paragraph().add(jobLot);
        doc.showTextAligned(lot, 400, 250, TextAlignment.CENTER, VerticalAlignment.MIDDLE);
        doc.add(new AreaBreak(AreaBreakType.NEXT_AREA));
        
        
        Iterator<Stage> it = job.getStages().iterator();
        int i = 0;
        while(it.hasNext())
        {
            i++;
            Stage stage = it.next();
            JFreeChart stageChart = stage.getChart();
            
            Image img = convertChartToImageLandscape(stageChart);
            //img.setAutoScaleWidth(true);
            img.scaleToFit(770, 260);
            img.setHorizontalAlignment(HorizontalAlignment.CENTER);
            doc.add(img);
            if(i == 2)
            {
                pdfDoc.addNewPage(PageSize.A4.rotate());
                i = 0;
            }
 
            
        }
        doc.close();   
        return pdfName;
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
    
    private Image convertChartToImageLandscape(JFreeChart chart) throws IOException {
        Image result = null;
        BufferedImage original = chart.createBufferedImage(800,260);
        
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(original, "png", os);
            os.flush();
            
            ImageData image = ImageDataFactory.create(os.toByteArray());
            result = new Image(image);
            //result.scaleToFit(PageSize.A4.rotate().getWidth(), PageSize.A4.rotate().getHeight() / 2);
        }
        return result;
    }
    
    public JPanel getDeleteStageDataPanel() {
        return deleteStageDataPanel;
    }
    
    private void addChartOptionsToPopupMenu(ChartPanel chartPanel) {
        JMenuItem deleteAnnotationItem = new JMenuItem("Delete Comment(s)");
        chartPanel.getPopupMenu().add(deleteAnnotationItem);
        deleteAnnotationItem.addActionListener((ev) -> {
           deleteAnnotation(chartPanel);
            
        });
        
        
        JMenuItem createNewStageItem = new JMenuItem("Create Stage");
        chartPanel.getPopupMenu().add(createNewStageItem);
        createNewStageItem.addActionListener((event) -> {
            XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = 0;
            try {
                x = xAxis.java2DToValue(chartPanel.getMousePosition().getX(), chartPanel.getScreenDataArea(), RectangleEdge.BOTTOM);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(null, "Incorrect Date Selected, Please Try Again!");
            }
            if(x != 0)
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = new Date((long)x);
                Stage stage = createNewStage(date);
                currentStage = stage.getStageNumber();
                ChartPanel cPanel = new ChartPanel(stage.getChart());
                setupChartPanel(cPanel);
            }
        });
        
        
        chartPanel.getPopupMenu().addSeparator();
        JMenuItem clearAllBeforeDateMenuItem = new JMenuItem("Remove All Previous");
        chartPanel.getPopupMenu().add(clearAllBeforeDateMenuItem);
        clearAllBeforeDateMenuItem.addActionListener((event) -> {
            XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = 0;
            try {
                x = xAxis.java2DToValue(chartPanel.getMousePosition().getX(), chartPanel.getScreenDataArea(), RectangleEdge.BOTTOM);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(null, "Incorrect Date Selected, Please Try Again!");
            }
            if(x != 0)
            {
                Date date = new Date((long)x);
                if(job.getStage(currentStage).deleteAllRecordingsBefore(date))
                {
                    ChartPanel cPanel = new ChartPanel(job.getStage(currentStage).getChart());
                    setupChartPanel(cPanel);
                }
            }
        });
        
        JMenuItem clearAllAfterDateMenuItem = new JMenuItem("Remove All After");
        chartPanel.getPopupMenu().add(clearAllAfterDateMenuItem);
        clearAllAfterDateMenuItem.addActionListener((event) -> {
             XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = 0;
            try {
                x = xAxis.java2DToValue(chartPanel.getMousePosition().getX(), chartPanel.getScreenDataArea(), RectangleEdge.BOTTOM);
            } catch(Exception e) {
                JOptionPane.showMessageDialog(null, "Incorrect Date Selected, Please Try Again!");
            }
            if(x != 0)
            {
                Date date = new Date((long)x);
                if(job.getStage(currentStage).deleteAllRecordingsAfter(date))
                {
                    ChartPanel cPanel = new ChartPanel(job.getStage(currentStage).getChart());
                    setupChartPanel(cPanel);
                }
            }
        });
        
        
    }
    
    private void setupChartPanel() {
        stageChartPanel = new JPanel();
        Stage first = job.getStages().get(0);
        currentStage = first.getStageNumber();
        ChartPanel chartPanel = new ChartPanel(first.getChart());
        chartPanel.setPreferredSize(new Dimension(850,650));
       
        
        addChartOptionsToPopupMenu(chartPanel);
        setupChartListener(chartPanel);
        stageChartPanel.add(chartPanel);
        
        
    }
    
    private void addNote(ChartPanel chartPanel, double x , double y) {
        JFreeChart chart = chartPanel.getChart();
        XYPlot plot = (XYPlot) chartPanel.getChart().getXYPlot();
        double yValue = DatasetUtilities.findYValue(plot.getDataset(0), 0, x);
        XYAnnotation annotation = null;
        HashMap<String,Object> annotationOptions = showNoteOptions();
        String note = (String)annotationOptions.get("comment");
        boolean pointer = (boolean) annotationOptions.get("pointer");
        boolean line = (boolean) annotationOptions.get("line");
        if(pointer) 
        {
            if(y < yValue)
                annotation = new XYPointerAnnotation(note,x,y,Math.toRadians(90));
            else
                annotation = new XYPointerAnnotation(note,x,y,Math.toRadians(270));
            ((XYPointerAnnotation)annotation).setFont(annotationFont);
            ((XYPointerAnnotation)annotation).setPaint(Color.RED.darker());
            
            ((XYPointerAnnotation)annotation).setArrowPaint(Color.RED.darker());
            ((XYPointerAnnotation)annotation).setArrowWidth(6);
            plot.addAnnotation(annotation);
            job.getStage(currentStage).addAnnotationToChart(annotation);
            
        }
        if (line)
        {
            XYLineAnnotation lineAnnotation = new XYLineAnnotation(x, 0.0, x, y, dashed, Color.RED.darker());
            plot.addAnnotation(lineAnnotation);
            job.getStage(currentStage).addAnnotationToChart(lineAnnotation);
                
        }
        if(!pointer && !line) {
            annotation = new XYTextAnnotation(note, x, y);
            ((XYTextAnnotation)annotation).setFont(annotationFont);
            plot.addAnnotation(annotation);
            job.getStage(currentStage).addAnnotationToChart(annotation);
        }
        
        

    }
    
    private void setupChartPanel(ChartPanel chartPanel) {
        stageChartPanel.remove(0);
        chartPanel.setPreferredSize(new Dimension(850,650));
        addChartOptionsToPopupMenu(chartPanel);
        
        setupChartListener(chartPanel);
        stageChartPanel.add(chartPanel);
        stageChartPanel.revalidate();
    }
    
    private void deleteAnnotation(ChartPanel chartPanel) {
        int size = job.getStage(currentStage).getAnnotations().size();
            if(size == 0)
                return;
            //XYTextAnnotation[] choices = new XYTextAnnotation[size];
            HashMap<String,XYAnnotation> map = new HashMap<>();
            String[] choices = new String[size];
            int i = 0;
            for(XYAnnotation ann : job.getStage(currentStage).getAnnotations())
            {
                if(ann instanceof XYTextAnnotation)
                {
                    choices[i] = ((XYTextAnnotation)ann).getText();
                    map.put(((XYTextAnnotation)ann).getText(), ann);
                    i++;
                }
                else if (ann instanceof XYPointerAnnotation)
                {
                    choices[i] = ((XYPointerAnnotation)ann).getText();
                    map.put(((XYPointerAnnotation)ann).getText(), ann);
                }
            }
            String ann = (String) JOptionPane.showInputDialog(null, "Select a comment to delete", "Delete Comment", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
            
            if(job.getStage(currentStage).deleteAnnotation(map.get(ann)))   
            {
                chartPanel = new ChartPanel(job.getStage(currentStage).getChart());
                setupChartPanel(chartPanel);
                
            }
    }
    
    public Container setupButtonPane() {
       deleteStageDataPanel = new JPanel(new GridBagLayout());
       GridBagConstraints gb = new GridBagConstraints();
       gb.insets = new Insets(5,10,5,10);
       gb.gridx = 0; 
       gb.gridy = 0;
       deleteStageDataPanel.add(overWriteButton, gb);
       
       gb.gridx = 1;
       gb.gridy = 0;
       deleteStageDataPanel.add(printPortraitButton, gb);
       
       gb.gridx = 2;
       gb.gridy = 0;
       deleteStageDataPanel.add(printLandscapeButton, gb);
       
       overWriteButton.addActionListener((event) -> {
           if(saveChangesToCSV())
               JOptionPane.showMessageDialog(null, "CSV Successfully Overwritten!");
       }); 
        printPortraitButton.addActionListener((event) -> {
            try {
                String pdfFilePath = createPortraitPDF();
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe","/C",pdfFilePath);
                Process p = processBuilder.start();
                
                
            } catch(Exception e) {
                
            }
        });
        
        
        printLandscapeButton.addActionListener((event) -> {
            try {
                String pdfFilePath = createLandscapePDF();
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe","/C",pdfFilePath);
                Process p = processBuilder.start();
                
                
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
        return returnPanel;
    }
    
    private JPanel setupStageButtons() {
        stageButtonsPanel = new JPanel(new GridLayout(0,2,2,2));
        for(Stage stage : job.getStages())
        {
            JButton button = job.getStageButton(stage);
            setupButtonEvent(button);
            stageButtonsPanel.add(button);
            
        }
        
        return stageButtonsPanel;
    }
    
    private void setupButtonEvent(JButton button) {
        button.addActionListener((e) -> {
            String bName = button.getText();
            String[] splitString = bName.split("\\s");
            int stageNumber;
            if(splitString[1].contains("."))
                stageNumber = Integer.parseInt(button.getName());
            else
                stageNumber = Integer.parseInt(splitString[1]);
            
            currentStage = stageNumber;
            Stage stage = job.getStage(stageNumber);
            ChartPanel chartPanel = new ChartPanel(stage.getChart());
            setupChartPanel(chartPanel);
            //stageChartPanel.add(chartPanel);
            //stageChartPanel.revalidate();
        });
    }
    
    HashMap<Point2D,XYPointerAnnotation> pTa = new HashMap<>();
    
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
        job.setJobID(jobID);
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
    
    private HashMap<String,Object> showNoteOptions() {
        HashMap<String,Object> map = new HashMap<>();
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel commentLabel = new JLabel("Enter Comment:");
        JTextField commentField = new JTextField(15);
        
        JCheckBox pointerCheckBox = new JCheckBox("Show Pointer");
        JCheckBox lineCheckBox = new JCheckBox("Show line");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPane.add(commentLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPane.add(commentField, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(pointerCheckBox, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentPane.add(lineCheckBox, gbc);
        
        int result = JOptionPane.showConfirmDialog(null, contentPane, "Comment Options", JOptionPane.OK_CANCEL_OPTION);
        
        if(result == JOptionPane.OK_OPTION)
        {
            String comment = commentField.getText();
            boolean pointer = pointerCheckBox.isSelected();
            boolean line = lineCheckBox.isSelected();
            map.put("comment", comment);
            map.put("pointer",pointer);
            map.put("line",line);
        }
        
        return map;
    }
    
    @Override
    public String toString() {
        return job.toString();
    }

    private Stage createNewStage(Date date) {
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        JPanel contentPane = new JPanel(new GridBagLayout());
        JLabel stageNumberLabel = new JLabel("Enter Substage #"), startTimeLabel = new JLabel("Start Time:"), endTimeLabel = new JLabel("End Time:");
        JTextField stageNumberField = new JTextField(2), startTimeField = new JTextField(10), endTimeField = new JTextField(10);
        GridBagConstraints gbc = new GridBagConstraints();
        String d = s.format(date);
        
        gbc.gridx =0;
        gbc.gridy =0;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(stageNumberLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(stageNumberField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(startTimeLabel, gbc);
         
        gbc.gridx = 0;
        gbc.gridy = 2;
        startTimeField.setText(d);
        contentPane.add(startTimeField, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentPane.add(endTimeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        endTimeField.setText(d);
        contentPane.add(endTimeField, gbc);
        
        int result = JOptionPane.showConfirmDialog(null, contentPane, "Comment Options", JOptionPane.OK_CANCEL_OPTION);
        Date start = null;
        Date end = null;
        Stage newStage = null;
        if(result == JOptionPane.OK_OPTION)
        {
            try{
                start = formatter.parse(startTimeField.getText());
                end = formatter.parse(endTimeField.getText()); 
            } catch(Exception e) {  
            }
            if(start != null && end != null)
            {
                ArrayList<Recording>  records = job.getStage(currentStage).getRecordingsBetween(start, end);
                newStage = new Stage(job.getNumStages() + 1, stageNumberField.getText(), records);
                job.addStage(newStage);
                JButton newStageButton = job.getStageButton(newStage);
                setupButtonEvent(newStageButton);
                stageButtonsPanel.add(newStageButton);
                stageButtonsPanel.revalidate();
                
            }
            
        }
        return newStage;
    }
}

