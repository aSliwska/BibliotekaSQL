package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.EditForm;
import org.example.gui.editform.PaymentEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenreToBookProfile extends Profile {

    private int ksiazka_id, gatunek_id;
    private String tytul, nazwa, drzewo;

    public GenreToBookProfile(int ksiazka_id, int gatunek_id) {
        super(new BorderLayout(), ksiazka_id, "gatunek_ksiazki");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("ID książki: " + ksiazka_id)));
        leftPane.add(alignLeft(new JLabel("Tytuł: " + tytul)));
        leftPane.add(alignLeft(new JLabel("ID gatunku: " + gatunek_id)));
        leftPane.add(alignLeft(new JLabel("Nazwa gatunku: " + nazwa)));
        leftPane.add(alignLeft(new JLabel("Drzewo gatunku: " + drzewo)));

        constructProfile("Gatunek książki", "", false, null, leftPane, this, null);
    }

    @Override
    protected void constructProfile(String titleText, String nameText, boolean hasTable, JTable table, JPanel stringsPane, JPanel selfRef, EditForm form) {
        // typ profilu
        JLabel title = new JLabel(titleText);
        title.setFont(UIManager.getDefaults().getFont("Label.font").deriveFont(Font.BOLD, 20));

        // usun przycisk
        JButton deleteButton = new JButton("Usuń");
        JPanel buttonWrapper = new JPanel(new FlowLayout());
        buttonWrapper.add(deleteButton);

        // wrapper dla naglowka
        JPanel naglowekWrapper = new JPanel(new BorderLayout());
        naglowekWrapper.add(title, BorderLayout.LINE_START);
        naglowekWrapper.add(buttonWrapper, BorderLayout.LINE_END);

        // imie nazwisko
        JLabel name = new JLabel(nameText);
        name.setBorder(BorderFactory.createEmptyBorder(5,15,10,0));
        name.setFont(UIManager.getDefaults().getFont("Label.font").deriveFont(Font.BOLD, 40));

        // gatunki table
        JPanel tableWrapper = null;
        if (hasTable) {
            tableWrapper = new JPanel(new BorderLayout());
            tableWrapper.add(table.getTableHeader(), BorderLayout.PAGE_START);
            tableWrapper.add(table, BorderLayout.CENTER);
        }

        // add everything
        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));
        leftPane.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        leftPane.add(naglowekWrapper);
        leftPane.add(alignLeft(name));
        leftPane.add(alignLeft(new JLabel("ID: " + String.valueOf(id))));
        leftPane.add(stringsPane);

        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.add(leftPane, BorderLayout.PAGE_START);
        if (hasTable)
            mainPane.add(tableWrapper, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        selfRef.add(scrollPane, BorderLayout.CENTER);

        deleteButton.addActionListener(e -> {
            delete();
            selfRef.remove(scrollPane);

            JLabel message = new JLabel("Usunięto rekord z ID = " + id);
            message.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

            selfRef.add(message, BorderLayout.PAGE_START);

            selfRef.invalidate();
            selfRef.validate();
            selfRef.repaint();
        });
    }

    @Override
    protected void delete() {
        JDBC db = new JDBC();
        String SQL = "DELETE FROM gatunek_ksiazki WHERE ksiazka_id = ? AND gatunek_id = ?";

        int affectedrows = 0;

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, ksiazka_id);
            pstmt.setInt(2, gatunek_id);

            affectedrows = pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

//        System.out.println("usunieto rekordow: " + affectedrows);
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
                SELECT ksiazka_id, tytul, gatunek_id, nazwa, znajdz_drzewo_gatunku_wstecz(gatunek_id) AS drzewo
                FROM gatunek_ksiazki JOIN ksiazka USING (ksiazka_id) JOIN gatunek USING (gatunek_id)
                WHERE ksiazka_id = ? AND gatunek_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, ksiazka_id);
            pstmt.setInt(2, gatunek_id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            ksiazka_id = rs.getInt("ksiazka_id");
            tytul = rs.getString("tytul");
            gatunek_id = rs.getInt("gatunek_id");
            nazwa = rs.getString("nazwa");
            drzewo = rs.getString("drzewo");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}