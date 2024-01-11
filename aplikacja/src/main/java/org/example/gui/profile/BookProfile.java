package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.BookEditForm;
import org.example.gui.editform.ReaderEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class BookProfile extends Profile {

    private String tytul, opis;
    private int wydawnictwo_id, autor_id, ilosc_egzemplarzy_w_systemie, ilosc_dostepnych_egzemplarzy;
    private ArrayList<String> drzewa = new ArrayList<>();
    private JTable egzemplarze;

    public BookProfile(int id) {
        super(new BorderLayout(), id, "ksiazka");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("Opis: " + opis)));
        leftPane.add(alignLeft(new JLabel("ID wydawnictwa: " + wydawnictwo_id)));
        leftPane.add(alignLeft(new JLabel("ID autora: " + autor_id)));
        leftPane.add(alignLeft(new JLabel("Ilość egzemplarzy w systemie: " + ilosc_egzemplarzy_w_systemie)));
        leftPane.add(alignLeft(new JLabel("Ilość dostępnych egzemplarzy: " + ilosc_dostepnych_egzemplarzy)));
        leftPane.add(alignLeft(new JLabel("Gatunki: ")));
        for (String drzewo : drzewa)
            leftPane.add(alignLeft(new JLabel("\t" + drzewo)));
        leftPane.add(alignLeft(new JLabel("Egzemplarze (i ich najnowsze wypożyczenia):")));

        constructProfile("Książka", tytul, true, egzemplarze, leftPane, this, new BookEditForm(id));
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
                SELECT tytul, opis, wydawnictwo_id, autor_id, ilosc_egzemplarzy_w_systemie, ilosc_dostepnych_egzemplarzy
                FROM ksiazka LEFT JOIN ksiazka_statystyki USING (ksiazka_id)
                WHERE ksiazka_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            tytul = rs.getString("tytul");
            opis = rs.getString("opis");
            wydawnictwo_id = rs.getInt("wydawnictwo_id");
            autor_id = rs.getInt("autor_id");
            ilosc_egzemplarzy_w_systemie = rs.getInt("ilosc_egzemplarzy_w_systemie");
            ilosc_dostepnych_egzemplarzy = rs.getInt("ilosc_dostepnych_egzemplarzy");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // wszystkie drzewa gatunkow ksiazki
        query = """
                SELECT znajdz_drzewo_gatunku_wstecz(gatunek_id) AS drzewo
                FROM gatunek_ksiazki
                WHERE ksiazka_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
                drzewa.add(rs.getString("drzewo"));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // egzemplarze
        String[] columnNames = {"ID egzemplarza", "ID wypożyczenia", "ID czytelnika", "Najnowsza data wypożyczenia", "Najnowsza data zwrotu", "Czy egzemplarz jest dostępny"};
        String[] dbNames = {"egzemplarz_id", "wypozyczenie_id", "czytelnik_id", "data_wypozyczenia", "data_zwrotu", "czy_egzemplarz_dostepny"};
        Object[][] rowData = {};
        query = """
                SELECT egzemplarz_id, wypozyczenie_id, czytelnik_id, data_wypozyczenia, data_zwrotu, CASE WHEN egzemplarz_id IN (SELECT egzemplarz_id FROM dostepne_egzemplarze) THEN 'tak' ELSE 'nie' END AS czy_egzemplarz_dostepny
                FROM egzemplarz LEFT JOIN najnowsze_wypozyczenie USING (egzemplarz_id)
                WHERE ksiazka_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            rowData = formatToRows(rs, dbNames);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
//        System.out.println(Arrays.deepToString(rowData));

        egzemplarze = new JTable(createTableModel(columnNames, rowData));
    }
}