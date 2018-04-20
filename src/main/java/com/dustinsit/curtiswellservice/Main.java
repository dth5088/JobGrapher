/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author dth5088
 */
public class Main {
    static Parser parser;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                parser = new Parser();
            
                try {
                        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                            if ("Nimbus".equals(info.getName())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                break;
                            }
                        }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
                    // If Nimbus is not available, you can set the GUI to another look and feel.
                }

                try {
                    parser.readFile(getSelectedFile());
                    MainFrame mainFrame = new MainFrame();
                }
                catch(FileNotFoundException e) {

                }
            }
        });
    }
    
    private static File getSelectedFile() {
        File file = null;
        String home = System.getProperty("user.home");
        JFileChooser fileChooser = new JFileChooser(home);
        fileChooser.setDialogTitle("Select a generated text file to start!");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        else
            System.exit(0);
        return file;
    }
    
    private static class MainFrame extends JFrame {
        JTextArea textArea = new JTextArea();
        JButton deleteButton = new JButton("Delete");
        JTextField startDateField,endDateField;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JLabel startLabel = new JLabel("Start Date:"), endLabel = new JLabel("End Date:");
        public MainFrame() {
            createAndShowGUI();
        }
        
         private void createAndShowGUI()
        {
            JFrame frame = new JFrame("Curtis Well Service");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            init(frame.getContentPane());
            frame.setPreferredSize(new Dimension(1200, 800));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            //World.setInit(); // World is initialized
        }
         
         private void init(Container contentPane) {
            createDisplay(contentPane);
            
        }
         
         public void updateTextArea(String str) {
             textArea.setText("");
             textArea.append(str);
         }
         
        private void createDisplay(Container contentPane) {
            contentPane.setLayout(new BorderLayout());
            //contentPane.add(textArea, BorderLayout.SOUTH);
            JScrollPane scrollPane = new JScrollPane(parser.setupButtonPane());
            contentPane.add(scrollPane, BorderLayout.CENTER);
            contentPane.add(parser.getDeleteStageDataPanel(), BorderLayout.SOUTH);

            
            
            
        }
        
        
        
        
        

    }
}
