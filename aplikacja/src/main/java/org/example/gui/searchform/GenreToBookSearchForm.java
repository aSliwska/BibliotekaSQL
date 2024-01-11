package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class GenreToBookSearchForm extends SearchForm {
    public GenreToBookSearchForm() {
        super(new GridLayout(2, 4, 10, 10));

        profileType = ProfileType.GENRE_TO_BOOK;

        mainTable = new SearchForm.TableName("gatunek_ksiazki", "gk");
        TableName ksiazka_table = new TableName("ksiazka", "k", "ksiazka_id");
        TableName gatunek_table = new TableName("gatunek", "g", "gatunek_id");
        additionalJoinTables.add(ksiazka_table);
        additionalJoinTables.add(gatunek_table);

        orderByField = new Field("ID książki", "ksiazka_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new Field("ID gatunku", "gatunek_id", DataType.INTEGER, mainTable));
        fields.add(new Field("Tytuł książki", "tytul", DataType.VARCHAR, ksiazka_table));
        fields.add(new Field("Nazwa gatunku", "nazwa", DataType.VARCHAR, gatunek_table));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}