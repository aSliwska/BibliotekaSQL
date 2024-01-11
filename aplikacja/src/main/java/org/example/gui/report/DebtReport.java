package org.example.gui.report;

import org.example.JDBC;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DebtReport extends Report {

    @Override
    public JTable fetch() {
        JDBC db = new JDBC();

        String[] columnNames = {"ID czytelnika", "Balans"};
        String[] dbNames = {"czytelnik_id", "balans"};
        Object[][] rowData = {};

        String query = """
                SELECT czytelnik_id, balans
                FROM balans_czytelnika
                WHERE balans < 0
                ORDER BY balans""";

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
