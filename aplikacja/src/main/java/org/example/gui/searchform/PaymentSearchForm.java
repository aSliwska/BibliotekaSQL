package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class PaymentSearchForm extends SearchForm {
    public PaymentSearchForm() {
        super(new GridLayout(2, 6, 10, 10));

        profileType = ProfileType.PAYMENT;

        mainTable = new TableName("wplata", "w");
        TableName czytelnik_table = new TableName("czytelnik", "c", "czytelnik_id");
        additionalJoinTables.add(czytelnik_table);

        orderByField = new SearchForm.Field("ID wpłaty", "wplata_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new SearchForm.Field("Data wpłaty", "data_wplaty", DataType.TIMESTAMP, mainTable));
        fields.add(new SearchForm.Field("Kwota", "kwota", DataType.FLOAT, mainTable));
        fields.add(new SearchForm.Field("ID czytelnika", "czytelnik_id", DataType.INTEGER, mainTable));
        fields.add(new SearchForm.Field("Imię czytelnika", "imie", DataType.VARCHAR, czytelnik_table));
        fields.add(new SearchForm.Field("Nazwisko czytelnika", "nazwisko", DataType.VARCHAR, czytelnik_table));

        for (SearchForm.Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}
