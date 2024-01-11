package org.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainPane extends JPanel {

    TitledContentPane titledContentPane;

    public MainPane() {
        super(new BorderLayout());

        // stworz glowne menu nawigacji
        JPanel menuPane = new JPanel();
        menuPane.setLayout(new GridBagLayout());
        menuPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPane.setBackground(new Color(180, 214, 206));
        menuPane.setBorder(BorderFactory.createEmptyBorder(0,15,0,15));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;

        menuPane.add(Box.createVerticalGlue());
        addButton("Szukaj", menuPane, constraints, TitledContentPane.LibraryCards.SEARCH);
        constraints.insets = new Insets(10,0,0,0);
        addButton("Dodaj", menuPane, constraints, TitledContentPane.LibraryCards.ADD);
        addButton("Wypożycz/zwróć", menuPane, constraints, TitledContentPane.LibraryCards.BORROW);
        addButton("Wpłać", menuPane, constraints, TitledContentPane.LibraryCards.PAY);
        addButton("Raporty", menuPane, constraints, TitledContentPane.LibraryCards.REPORTS);
        menuPane.add(Box.createVerticalGlue());

        // stworz kontener wrapper z naglowkiem
        titledContentPane = new TitledContentPane();

        // dodaj wszystko do glownego kontenera
        this.add(menuPane, BorderLayout.LINE_START);
        this.add(titledContentPane, BorderLayout.CENTER);
    }

    private void addButton(String text, Container container, GridBagConstraints constraints, TitledContentPane.LibraryCards cardType) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(130, 50));

        button.addActionListener(e -> titledContentPane.switchToPane(cardType));

        container.add(button, constraints);
    }
}
