package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class AuthorSearchForm extends SearchForm {
    public AuthorSearchForm() {
        super(new GridLayout(1, 6, 10, 10));

        profileType = ProfileType.AUTHOR;

        mainTable = new TableName("autor", "a");

        orderByField = new Field("ID autora", "autor_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new Field("Imię autora", "imie", DataType.VARCHAR, mainTable));
        fields.add(new Field("Nazwisko autora", "nazwisko", DataType.VARCHAR, mainTable));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}