package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.PublisherEditForm;
import org.example.gui.editform.ReaderEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;

public class PublisherProfile extends Profile {

    private String nazwa;
    private Date data_zalozenia;
    private int ilosc_ksiazek;
    private JTable gatunki;

    public PublisherProfile(int id) {
        super(new BorderLayout(), id, "wydawnictwo");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("Data założenia: " + data_zalozenia)));
        leftPane.add(alignLeft(new JLabel("Ilość książek: " + ilosc_ksiazek)));
        leftPane.add(alignLeft(new JLabel("Gatunki, które pojawiały się w wydawanych przez wydawnictwo książkach:")));

        constructProfile("Wydawnictwo", nazwa, true, gatunki, leftPane, this, new PublisherEditForm(id));
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
                WITH statystyki_ksiazkowe AS (
                    SELECT wydawnictwo_id, COUNT(DISTINCT ksiazka_id) AS ilosc_ksiazek
                    FROM ksiazka
                    GROUP BY wydawnictwo_id
                )
                SELECT nazwa, data_zalozenia, ilosc_ksiazek
                FROM wydawnictwo LEFT JOIN statystyki_ksiazkowe USING (wydawnictwo_id)
                WHERE wydawnictwo_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            nazwa = rs.getString("nazwa");
            data_zalozenia = rs.getDate("data_zalozenia");
            ilosc_ksiazek = rs.getInt("ilosc_ksiazek");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // czestosc_czytanych_gatunkow
        String[] columnNames = {"Nazwa gatunku", "Ilość wystąpień"};
        String[] dbNames = {"nazwa", "czestosc"};
        Object[][] rowData = {};
        query = "SELECT nazwa, czestosc " +
                "FROM czestosc_gatunkow_dla_ksiazek('SELECT ksiazka_id FROM ksiazka WHERE wydawnictwo_id = "+id+"') JOIN gatunek USING (gatunek_id) " +
                "ORDER BY czestosc DESC";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            rowData = formatToRows(rs, dbNames);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
//        System.out.println(Arrays.deepToString(rowData));

        gatunki = new JTable(createTableModel(columnNames, rowData));
    }
}
