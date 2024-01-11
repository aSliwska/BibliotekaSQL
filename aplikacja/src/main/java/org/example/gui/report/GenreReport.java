package org.example.gui.report;

import org.example.JDBC;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenreReport extends Report {
    @Override
    public JTable fetch() {
        JDBC db = new JDBC();

        String[] columnNames = {"ID gatunku", "Nazwa", "Ilość książek pod tym gatunkiem"};
        String[] dbNames = {"gatunek_id", "nazwa", "czestosc"};
        Object[][] rowData = {};

        String query = """
                SELECT gatunek_id, nazwa, czestosc
                FROM czestosc_gatunkow_dla_ksiazek('SELECT ksiazka_id FROM ksiazka') JOIN gatunek USING (gatunek_id)
                ORDER BY czestosc DESC""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            rowData = formatToRows(rs, dbNames);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
//        System.out.println(Arrays.deepToString(rowData));

        return new JTable(createTableModel(columnNames, rowData));
    }
}
