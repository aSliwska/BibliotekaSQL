package org.example.gui.addform;

import org.example.gui.searchform.SearchForm;
import org.example.gui.searchform.Searchable;

import javax.swing.*;
import java.awt.*;

public class AuthorAddForm extends AddForm {
    public AuthorAddForm() {
        super(new GridLayout(1, 4, 10, 10));

        tableName = "autor";

        fields.add(new Field("Imię autora", "imie", DataType.VARCHAR));
        fields.add(new Field("Nazwisko autora", "nazwisko", DataType.VARCHAR));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}