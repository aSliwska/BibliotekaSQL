package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class BorrowLogSearchForm extends SearchForm {
    public BorrowLogSearchForm() {
        super(new GridLayout(3, 6, 10, 10));

        profileType = ProfileType.BORROW_LOG;

        mainTable = new SearchForm.TableName("wypozyczenie", "w");
        SearchForm.TableName czytelnik_table = new SearchForm.TableName("czytelnik", "c", "czytelnik_id");
        additionalJoinTables.add(czytelnik_table);

        TableName status_wypozyczenia_table = new TableName("status_wypozyczenia", "sw", "wypozyczenie_id", true);
        additionalJoinTables.add(status_wypozyczenia_table);
        additionalDisplayColumns.add(new Field("Ilość dni ponad limit czasowy", "dni_nad_limitem", null, DataType.INTEGER, status_wypozyczenia_table));

        orderByField = new SearchForm.Field("ID wypożyczenia", "wypozyczenie_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new SearchForm.Field("ID egzemplarza", "egzemplarz_id", DataType.INTEGER, mainTable));
        fields.add(new SearchForm.Field("ID czytelnika", "czytelnik_id", DataType.INTEGER, mainTable));
        fields.add(new SearchForm.Field("Imię czytelnika", "imie", DataType.VARCHAR, czytelnik_table));
        fields.add(new SearchForm.Field("Nazwisko czytelnika", "nazwisko", DataType.VARCHAR, czytelnik_table));
        fields.add(new SearchForm.Field("Data wypożyczenia", "data_wypozyczenia", DataType.TIMESTAMP, mainTable));
        fields.add(new SearchForm.Field("Data zwrotu", "data_zwrotu", DataType.TIMESTAMP, mainTable));

        for (SearchForm.Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }

        this.add(new JLabel("", SwingConstants.CENTER));
        this.add(new JLabel("", SwingConstants.CENTER));
        this.add(new JLabel("", SwingConstants.CENTER));
        this.add(new JLabel("", SwingConstants.CENTER));
    }
}