package org.example.gui.report;

import org.example.JDBC;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class ItemsOverTimeLimitReport extends Report {
    @Override
    public JTable fetch() {
        JDBC db = new JDBC();

        String[] columnNames = {"ID wypożyczenia", "ID czytelnika", "ID egzemplarza", "Dni, o które przekroczono limit czasowy"};
        String[] dbNames = {"wypozyczenie_id", "czytelnik_id", "egzemplarz_id", "dni_nad_limitem"};
        Object[][] rowData = {};

        String query = """
                SELECT wypozyczenie_id, w.czytelnik_id, w.egzemplarz_id, dlugosc_wypozyczenia-30 AS dni_nad_limitem
                FROM przedluzone_wypozyczenia JOIN wypozyczenie w USING (wypozyczenie_id)
                WHERE w.data_zwrotu IS NULL""";

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
