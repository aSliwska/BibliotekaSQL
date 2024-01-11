package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class ReaderSearchForm extends SearchForm {

    public ReaderSearchForm() {
        super(new GridLayout(2, 6, 10, 10));

        profileType = ProfileType.READER;

        mainTable = new TableName("czytelnik", "c");

        TableName balans_czytelnika_table = new TableName("balans_czytelnika", "bc", "czytelnik_id", true);
        TableName czytelnik_statystyki_table = new TableName("czytelnik_statystyki", "cs", "czytelnik_id", true);
        TableName status_bana_table = new TableName("status_bana", "sb", "czytelnik_id", true);
        additionalJoinTables.add(balans_czytelnika_table);
        additionalJoinTables.add(czytelnik_statystyki_table);
        additionalJoinTables.add(status_bana_table);
        additionalDisplayColumns.add(new Field("Balans", "balans", null, DataType.FLOAT, balans_czytelnika_table));
        additionalDisplayColumns.add(new Field("Ilość aktualnie wypożyczonych egzemplarzy", "ilosc_aktualnie_wypozyczonych", null, DataType.INTEGER, czytelnik_statystyki_table));
        additionalDisplayColumns.add(new Field("Ilość aktualnie nieoddanych egzemplarzy po terminie", "ilosc_aktualnie_po_terminie", null, DataType.INTEGER, czytelnik_statystyki_table));
        additionalDisplayColumns.add(new Field("Czy jest zbanowany", "czy_jest_zbanowany", null, DataType.VARCHAR, status_bana_table));

        orderByField = new Field("ID czytelnika", "czytelnik_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new Field("Imię", "imie", DataType.VARCHAR, mainTable));
        fields.add(new Field("Nazwisko", "nazwisko", DataType.VARCHAR, mainTable));
        fields.add(new Field("Email", "email", DataType.VARCHAR, mainTable));
        fields.add(new Field("Telefon", "telefon", DataType.VARCHAR, mainTable));
        fields.add(new Field("Data rejestracji", "data_rejestracji", DataType.DATE, mainTable));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}
