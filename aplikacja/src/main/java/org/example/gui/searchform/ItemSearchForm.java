package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class ItemSearchForm extends SearchForm {
    public ItemSearchForm() {
        super(new GridLayout(1, 6, 10, 10));

        profileType = ProfileType.GENRE_TO_BOOK;

        mainTable = new SearchForm.TableName("egzemplarz", "e");
        TableName ksiazka_table = new TableName("ksiazka", "k", "ksiazka_id");
        additionalJoinTables.add(ksiazka_table);

        orderByField = new Field("ID egzemplarza", "egzemplarz_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new Field("ID książki", "ksiazka_id", DataType.INTEGER, mainTable));
        fields.add(new Field("Tytuł książki", "tytul", DataType.VARCHAR, ksiazka_table));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}