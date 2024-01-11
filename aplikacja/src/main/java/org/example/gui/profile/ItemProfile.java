package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.ItemEditForm;
import org.example.gui.editform.PaymentEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ItemProfile extends Profile {

    private int ksiazka_id;
    private String tytul;

    public ItemProfile(int id) {
        super(new BorderLayout(), id, "egzemplarz");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("ID książki: " + ksiazka_id)));
        leftPane.add(alignLeft(new JLabel("tytul: " + tytul)));

        constructProfile("Egzemplarz", "", false, null, leftPane, this, new ItemEditForm(id));
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
            SELECT ksiazka_id, tytul
            FROM egzemplarz JOIN ksiazka USING (ksiazka_id)
            WHERE egzemplarz_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            ksiazka_id = rs.getInt("ksiazka_id");
            tytul = rs.getString("tytul");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}