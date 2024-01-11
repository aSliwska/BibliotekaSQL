package org.example.gui;

import org.example.gui.addform.AddForm;
import org.example.gui.addform.BorrowLogAddForm;
import org.example.gui.addform.PaymentAddForm;

import javax.swing.*;
import java.awt.*;

public class PayPane extends JPanel {
//    private final Color backgroundColor = new Color(200, 200, 200);

    public PayPane() {
        super(new BorderLayout());

        JPanel tab = new JPanel(new BorderLayout());
//        tab.setBackground(backgroundColor);

        // forma do wprowadzenia parametrow szukania
        PaymentAddForm form = new PaymentAddForm();
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
        this.add(scrollPane, BorderLayout.CENTER);
    }
}