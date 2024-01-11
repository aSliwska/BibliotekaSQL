package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.GenreEditForm;
import org.example.gui.editform.ReaderEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class GenreProfile extends Profile {

    private String nazwa, drzewo;
    private int gatunek_rodzic_id;
    private JTable gatunki_dzieci;

    public GenreProfile(int id) {
        super(new BorderLayout(), id, "gatunek");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("ID gatunku-rodzica: " + gatunek_rodzic_id)));
        leftPane.add(alignLeft(new JLabel("Drzewo gatunku: " + drzewo)));
        leftPane.add(alignLeft(new JLabel("Bezpośrednie dzieci gatunku:")));

        constructProfile("Gatunek", nazwa, true, gatunki_dzieci, leftPane, this, new GenreEditForm(id));
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
            SELECT nazwa, gatunek_rodzic_id, znajdz_drzewo_gatunku_wstecz(gatunek_id) AS drzewo
            FROM gatunek
            WHERE gatunek_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            nazwa = rs.getString("nazwa");
            gatunek_rodzic_id = rs.getInt("gatunek_rodzic_id");
            drzewo = rs.getString("drzewo");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // gatunki dzieci
        String[] columnNames = {"ID gatunku", "Nazwa gatunku"};
        String[] dbNames = {"gatunek_id", "nazwa"};
        Object[][] rowData = {};
        query = """
                SELECT gatunek_id, nazwa
                FROM gatunek
                WHERE gatunek_rodzic_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            rowData = formatToRows(rs, dbNames);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
//        System.out.println(Arrays.deepToString(rowData));

        gatunki_dzieci = new JTable(createTableModel(columnNames, rowData));
    }
}
