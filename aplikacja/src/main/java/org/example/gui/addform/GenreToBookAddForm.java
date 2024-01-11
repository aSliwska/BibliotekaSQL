package org.example.gui.addform;

import javax.swing.*;
import java.awt.*;

public class GenreToBookAddForm extends AddForm {
    public GenreToBookAddForm() {
        super(new GridLayout(1, 4, 10, 10));

        tableName = "gatunek_ksiazki";

        fields.add(new Field("ID książki", "ksiazka_id", DataType.INTEGER));
        fields.add(new Field("ID gatunku", "gatunek_id", DataType.INTEGER));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}