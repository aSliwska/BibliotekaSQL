package org.example.gui;

import org.example.gui.addform.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddPane extends JPanel {
//    private final Color backgroundColor = new Color(100, 100, 100);

    public AddPane() {
        super(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Czytelnik", getAddTab(new ReaderAddForm()));
        tabbedPane.addTab("Egzemplarz", getAddTab(new ItemAddForm()));
        tabbedPane.addTab("Książka", getAddTab(new BookAddForm()));
        tabbedPane.addTab("Autor", getAddTab(new AuthorAddForm()));
        tabbedPane.addTab("Wydawnictwo", getAddTab(new PublisherAddForm()));
        tabbedPane.addTab("Gatunek", getAddTab(new GenreAddForm()));
        tabbedPane.addTab("Gatunek danej książki", getAddTab(new GenreToBookAddForm()));

        this.add(tabbedPane, BorderLayout.CENTER);
    }

    public JScrollPane getAddTab(AddForm form) {
        JPanel tab = new JPanel(new BorderLayout());
//        tab.setBackground(backgroundColor);

        // forma do wprowadzenia danych
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.add(form, BorderLayout.LINE_START);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // guziki
        JButton addButton = new JButton("Dodaj");
        JButton clearButton = new JButton("Wyczyść");
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.add(addButton, BorderLayout.LINE_START);
        buttonWrapper.add(clearButton, BorderLayout.LINE_END);
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // powiadomienie zwrotne
        JLabel messageSpot = new JLabel("");
        JPanel messageWrapper = new JPanel(new BorderLayout());
        messageWrapper.add(messageSpot, BorderLayout.PAGE_START);
        messageWrapper.setBorder(BorderFactory.createEmptyBorder(15,15,0,15));

        // dodanie wszystkiego do panelu
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.PAGE_AXIS));
        wrapper.add(formWrapper);
        wrapper.add(buttonWrapper);

        tab.add(wrapper, BorderLayout.PAGE_START);
        tab.add(messageWrapper, BorderLayout.LINE_START);

        // dodanie funkcjonalnosci guzikom
        addButton.addActionListener(e -> {
            messageSpot.setText(form.insert());
            tab.invalidate();
            tab.validate();
        });
        clearButton.addActionListener(e -> form.clearFields());

        JScrollPane scrollPane = new JScrollPane(tab, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
}