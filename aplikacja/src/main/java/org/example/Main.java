package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.gui.MainPane;

import javax.swing.*;
import java.awt.*;

public class Main {

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Biblioteka SQL - Aleksandra Śliwska");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setContentPane(new MainPane());

        frame.setMinimumSize(new Dimension(1200, 500));
        frame.setSize(1200, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // set theme
        FlatLightLaf.setup();
        UIManager.put( "Table.alternateRowColor", new Color(240, 240, 240) );
        UIManager.put( "Table.selectionBackground", new Color(124, 161, 151) );

        //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(Main::createAndShowGUI);
    }
}