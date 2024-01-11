package org.example.gui.addform;

import javax.swing.*;
import java.awt.*;


public class GenreAddForm extends AddForm {
    public GenreAddForm() {
        super(new GridLayout(1, 4, 10, 10));

        tableName = "gatunek";

        fields.add(new Field("ID gatunku-rodzica", "gatunek_rodzic_id", DataType.INTEGER));
        fields.add(new Field("Nazwa gatunku", "nazwa", DataType.VARCHAR));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}