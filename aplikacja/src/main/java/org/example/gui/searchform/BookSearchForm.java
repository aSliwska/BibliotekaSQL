package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class BookSearchForm extends SearchForm {
    public BookSearchForm() {
        super(new GridLayout(3, 6, 10, 10));

        profileType = ProfileType.BOOK;

        mainTable = new TableName("ksiazka", "k");
        TableName autor_table = new TableName("autor", "a", "autor_id");
        TableName wydawnictwo_table = new TableName("wydawnictwo", "w", "wydawnictwo_id");
        TableName egzemplarz_table = new TableName("egzemplarz", "e", "ksiazka_id", true);
        additionalJoinTables.add(autor_table);
        additionalJoinTables.add(wydawnictwo_table);
        additionalJoinTables.add(egzemplarz_table);

        TableName ksiazka_statystyki_table = new TableName("ksiazka_statystyki", "ks", "ksiazka_id", true);
        additionalJoinTables.add(ksiazka_statystyki_table);
        additionalDisplayColumns.add(new Field("Ilość egzemplarzy w systemie", "ilosc_egzemplarzy_w_systemie", null, DataType.INTEGER, ksiazka_statystyki_table));
        additionalDisplayColumns.add(new Field("Ilość dostępnych egzemplarzy", "ilosc_dostepnych_egzemplarzy", null, DataType.INTEGER, ksiazka_statystyki_table));

        orderByField = new Field("ID książki", "ksiazka_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new Field("ID egzemplarza", "egzemplarz_id", DataType.INTEGER, egzemplarz_table));
        fields.add(new Field("Tytuł", "tytul", DataType.VARCHAR, mainTable));
        fields.add(new Field("Opis", "opis", DataType.VARCHAR, mainTable));
        fields.add(new Field("ID wydawnictwa", "wydawnictwo_id", DataType.INTEGER, mainTable));
        fields.add(new Field("Nazwa wydawnictwa", "nazwa", DataType.VARCHAR, wydawnictwo_table));
        fields.add(new Field("ID autora", "autor_id", DataType.INTEGER, mainTable));
        fields.add(new Field("Imię autora", "imie", DataType.VARCHAR, autor_table));
        fields.add(new Field("Nazwisko autora", "nazwisko", DataType.VARCHAR, autor_table));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}