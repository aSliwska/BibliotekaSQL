package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.PaymentEditForm;
import org.example.gui.editform.ReaderEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;

public class PaymentProfile extends Profile {

    private Timestamp data_wplaty;
    private float kwota;
    private int czytelnik_id;
    private String imie, nazwisko;

    public PaymentProfile(int id) {
        super(new BorderLayout(), id, "wplata");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("Data wpłaty: " + data_wplaty)));
        leftPane.add(alignLeft(new JLabel("ID czytelnika: " + czytelnik_id)));
        leftPane.add(alignLeft(new JLabel("Imię czytelnika: " + imie)));
        leftPane.add(alignLeft(new JLabel("Nazwisko czytelnika: " + nazwisko)));

        constructProfile("Wpłata", kwota + " zł", false, null, leftPane, this, new PaymentEditForm(id));
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
                SELECT data_wplaty, kwota, czytelnik_id, imie, nazwisko
                FROM wplata JOIN czytelnik USING (czytelnik_id)
                WHERE wplata_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            data_wplaty = rs.getTimestamp("data_wplaty");
            kwota = rs.getFloat("kwota");
            czytelnik_id = rs.getInt("czytelnik_id");
            imie = rs.getString("imie");
            nazwisko = rs.getString("nazwisko");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}