package org.example.gui;

import javax.swing.*;
import java.awt.*;

public class TitledContentPane extends JPanel {

    public static enum LibraryCards {
        DEFAULT("System biblioteczny SQL"), SEARCH("Wyszukiwanie"), ADD("Dodawanie do bazy"),
        BORROW("Wypożyczanie/zwracanie książki"), PAY("Dokonywanie płatności"), REPORTS("Raporty");

        private final String title;
        LibraryCards(String title) {
            this.title = title;
        }

        public String title() { return title; }
    }

    private JLabel title;
    private JPanel cards;

    public TitledContentPane() {
        super(new BorderLayout());

        // stworz naglowek
        title = new JLabel("System biblioteczny SQL");
        title.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        title.setFont(UIManager.getDefaults().getFont("Label.font").deriveFont(Font.BOLD, 20));

        // stworz kontener dla glownej zawartosci strony
        cards = new JPanel(new CardLayout());
        cards.add(getDefaultCard(), LibraryCards.DEFAULT.name());
        cards.add(new SearchPane(), LibraryCards.SEARCH.name());
        cards.add(new AddPane(), LibraryCards.ADD.name());
        cards.add(new BorrowPane(), LibraryCards.BORROW.name());
        cards.add(new PayPane(), LibraryCards.PAY.name());
        cards.add(new ReportPane(), LibraryCards.REPORTS.name());

        // dodaj zawartosc strony
        this.add(title, BorderLayout.PAGE_START);
        this.add(cards, BorderLayout.CENTER);
    }

    public void switchToPane(LibraryCards paneType) {
        this.title.setText(paneType.title);

        CardLayout layout = (CardLayout)(cards.getLayout());
        layout.show(cards, paneType.name());
    }

    private JPanel getDefaultCard() {
        JPanel defaultPane = new JPanel();
        defaultPane.setLayout(new BorderLayout());
//        defaultPane.setBackground(new Color(200, 200, 200));
        defaultPane.setBorder(BorderFactory.createEmptyBorder(15,15,0,15));

        JLabel subtitle = new JLabel("Kliknij guziki po lewej aby zacząć.");
        defaultPane.add(subtitle, BorderLayout.PAGE_START);

        return defaultPane;
    }
}
