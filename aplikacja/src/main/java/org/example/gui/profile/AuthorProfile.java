package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.AuthorEditForm;
import org.example.gui.editform.ReaderEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class AuthorProfile extends Profile {
    private String imie, nazwisko;
    private int ilosc_ksiazek, ilosc_egzemplarzy;
    private JTable gatunki;

    public AuthorProfile(int id) {
        super(new BorderLayout(), id, "autor");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("Ilość książek: " + ilosc_ksiazek)));
        leftPane.add(alignLeft(new JLabel("Ilość egzemplarzy: " + ilosc_egzemplarzy)));
        leftPane.add(alignLeft(new JLabel("Gatunki, które pojawiały się w pisanych przez autora książkach:")));

        constructProfile("Autor", imie + " " + nazwisko, true, gatunki, leftPane, this, new AuthorEditForm(id));
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
            WITH statystyki_ksiazkowe AS (
                SELECT autor_id,
                    CASE WHEN COUNT(DISTINCT ksiazka_id) IS NULL THEN 0 ELSE COUNT(DISTINCT ksiazka_id) END AS ilosc_ksiazek,
                    CASE WHEN COUNT(DISTINCT egzemplarz_id) IS NULL THEN 0 ELSE COUNT(DISTINCT egzemplarz_id) END AS ilosc_egzemplarzy
                FROM ksiazka LEFT JOIN egzemplarz USING (ksiazka_id)
                GROUP BY autor_id
            )
            SELECT imie, nazwisko, ilosc_ksiazek, ilosc_egzemplarzy
            FROM autor LEFT JOIN statystyki_ksiazkowe USING (autor_id)
            WHERE autor_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            imie = rs.getString("imie");
            nazwisko = rs.getString("nazwisko");
            ilosc_ksiazek = rs.getInt("ilosc_ksiazek");
            ilosc_egzemplarzy = rs.getInt("ilosc_egzemplarzy");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // czestosc_czytanych_gatunkow
        String[] columnNames = {"Nazwa gatunku", "Ilość wystąpień"};
        String[] dbNames = {"nazwa", "czestosc"};
        Object[][] rowData = {};
        query = "SELECT nazwa, czestosc " +
                "FROM czestosc_gatunkow_dla_ksiazek('SELECT ksiazka_id FROM ksiazka WHERE autor_id = "+id+"') JOIN gatunek USING (gatunek_id) " +
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
