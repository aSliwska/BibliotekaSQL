package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.BorrowLogEditForm;
import org.example.gui.editform.PaymentEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BorrowLogProfile extends Profile {

    private int egzemplarz_id, czytelnik_id;
    private String tytul, imie, nazwisko;
    private Timestamp data_wypozyczenia, data_zwrotu;

    public BorrowLogProfile(int id) {
        super(new BorderLayout(), id, "wypozyczenie");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("ID egzemplarza: " + egzemplarz_id)));
        leftPane.add(alignLeft(new JLabel("Tytuł książki: " + tytul)));
        leftPane.add(alignLeft(new JLabel("ID czytelnika: " + czytelnik_id)));
        leftPane.add(alignLeft(new JLabel("Imię czytelnika: " + imie)));
        leftPane.add(alignLeft(new JLabel("Nazwisko czytelnika: " + nazwisko)));
        leftPane.add(alignLeft(new JLabel("Data wypożyczenia: " + data_wypozyczenia)));
        leftPane.add(alignLeft(new JLabel("Data zwrotu: " + data_zwrotu)));

        constructProfile("Wypożyczenie", "", false, null, leftPane, this, new BorrowLogEditForm(id));
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
            SELECT egzemplarz_id, tytul, czytelnik_id, imie, nazwisko, data_wypozyczenia, data_zwrotu
            FROM wypozyczenie JOIN czytelnik USING (czytelnik_id) LEFT JOIN egzemplarz USING (egzemplarz_id) LEFT JOIN ksiazka USING (ksiazka_id)
            WHERE wypozyczenie_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            egzemplarz_id = rs.getInt("egzemplarz_id");
            tytul = rs.getString("tytul");
            czytelnik_id = rs.getInt("czytelnik_id");
            imie = rs.getString("imie");
            nazwisko = rs.getString("nazwisko");
            data_wypozyczenia = rs.getTimestamp("data_wypozyczenia");
            data_zwrotu = rs.getTimestamp("data_zwrotu");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}