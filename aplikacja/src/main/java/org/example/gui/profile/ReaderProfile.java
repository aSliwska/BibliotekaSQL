package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.PublisherEditForm;
import org.example.gui.editform.ReaderEditForm;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;

public class ReaderProfile extends Profile {

    private String imie, nazwisko, email, telefon, czy_jest_zbanowany;
    private Date data_rejestracji;
    private float balans;
    private int ilosc_aktualnie_wypozyczonych, ilosc_aktualnie_po_terminie, ogolna_ilosc_wypozyczen, suma_przedluzen;
    private JTable gatunki;

    public ReaderProfile(int id) {
        super(new BorderLayout(), id, "czytelnik");

        fetchData();

        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));

        leftPane.add(alignLeft(new JLabel("Email: " + email)));
        leftPane.add(alignLeft(new JLabel("Telefon: " + telefon)));
        leftPane.add(alignLeft(new JLabel("Data rejestracji: " + data_rejestracji)));
        leftPane.add(alignLeft(new JLabel("Balans: " + balans + " zł")));
        leftPane.add(alignLeft(new JLabel("Czy jest zbanowany: " + czy_jest_zbanowany)));
        leftPane.add(alignLeft(new JLabel("Ilość aktualnie wypożyczonych egzemplarzy: " + ilosc_aktualnie_wypozyczonych)));
        leftPane.add(alignLeft(new JLabel("Ilość wypożyczonych egzemplarzy, których termin wypożyczenia minął: " + ilosc_aktualnie_po_terminie)));
        leftPane.add(alignLeft(new JLabel("Całkowita ilość wypożyczeń (od momentu rejestracji): " + ogolna_ilosc_wypozyczen)));
        leftPane.add(alignLeft(new JLabel("Suma dni, w których trzymał egzemplarz po terminie: " + suma_przedluzen)));
        leftPane.add(alignLeft(new JLabel("Gatunki, które pojawiały się w czytanych przez czytelnika książkach:")));

        constructProfile("Czytelnik", imie + " " + nazwisko, true, gatunki, leftPane, this, new ReaderEditForm(id));
    }

    @Override
    protected void fetchData() {
        JDBC db = new JDBC();

        // pojedyncze dane
        String query = """
                WITH zsumowana_ilosc_wypozyczen AS (
                    SELECT czytelnik_id, COUNT(*) AS ogolna_ilosc_wypozyczen
                    FROM wypozyczenie
                    GROUP BY czytelnik_id
                ), zsumowana_ilosc_dni_nad_termin AS (
                    SELECT czytelnik_id, SUM(dlugosc_wypozyczenia-30) as suma_przedluzen
                    FROM przedluzone_wypozyczenia
                    GROUP BY czytelnik_id
                )
                SELECT imie, nazwisko, email, telefon, data_rejestracji, balans, czy_jest_zbanowany, ilosc_aktualnie_wypozyczonych, ilosc_aktualnie_po_terminie, ogolna_ilosc_wypozyczen, suma_przedluzen
                FROM czytelnik LEFT JOIN balans_czytelnika USING (czytelnik_id) LEFT JOIN status_bana USING (czytelnik_id) LEFT JOIN czytelnik_statystyki USING (czytelnik_id) LEFT JOIN zsumowana_ilosc_wypozyczen USING (czytelnik_id) LEFT JOIN zsumowana_ilosc_dni_nad_termin USING (czytelnik_id)
                WHERE czytelnik_id = ?""";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            rs.next();

            imie = rs.getString("imie");
            nazwisko = rs.getString("nazwisko");
            email = rs.getString("email");
            telefon = rs.getString("telefon");
            data_rejestracji = rs.getDate("data_rejestracji");
            balans = rs.getFloat("balans");
            czy_jest_zbanowany = rs.getString("czy_jest_zbanowany");
            ilosc_aktualnie_wypozyczonych = rs.getInt("ilosc_aktualnie_wypozyczonych");
            ilosc_aktualnie_po_terminie = rs.getInt("ilosc_aktualnie_po_terminie");
            ogolna_ilosc_wypozyczen = rs.getInt("ogolna_ilosc_wypozyczen");
            suma_przedluzen = rs.getInt("suma_przedluzen");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // czestosc_czytanych_gatunkow
        String[] columnNames = {"Nazwa gatunku", "Ilość wystąpień"};
        String[] dbNames = {"nazwa", "czestosc"};
        Object[][] rowData = {};
        query = "SELECT nazwa, czestosc " +
                "FROM czestosc_gatunkow_dla_ksiazek('SELECT DISTINCT ksiazka_id FROM ksiazka JOIN egzemplarz USING (ksiazka_id) JOIN wypozyczenie USING (egzemplarz_id) " +
                "WHERE czytelnik_id = "+id+"') JOIN gatunek USING (gatunek_id) " +
                "ORDER BY czestosc DESC";

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

//            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            rowData = formatToRows(rs, dbNames);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
//        System.out.println(Arrays.deepToString(rowData));

        gatunki = new JTable(createTableModel(columnNames, rowData));
    }
}
