package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class GenreSearchForm extends SearchForm {
    public GenreSearchForm() {
        super(new GridLayout(1, 4, 10, 10));

        profileType = ProfileType.GENRE;

        mainTable = new TableName("gatunek", "g");

        orderByField = new SearchForm.Field("ID gatunku", "gatunek_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new SearchForm.Field("Nazwa gatunku", "nazwa", DataType.VARCHAR, mainTable));

        for (SearchForm.Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}