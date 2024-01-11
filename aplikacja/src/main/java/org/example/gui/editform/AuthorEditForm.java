package org.example.gui.editform;

import org.example.gui.addform.AddForm;

import javax.swing.*;
import java.awt.*;

public class AuthorEditForm extends EditForm {
    public AuthorEditForm(int id) {
        super(new GridLayout(1, 4, 10, 10), id);

        tableName = "autor";
        idDbColumnName = "autor_id";

        fields.add(new Field("Imię autora", "imie", DataType.VARCHAR));
        fields.add(new Field("Nazwisko autora", "nazwisko", DataType.VARCHAR));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}
