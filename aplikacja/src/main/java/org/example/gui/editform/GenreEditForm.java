package org.example.gui.editform;

import org.example.gui.addform.AddForm;

import javax.swing.*;
import java.awt.*;

public class GenreEditForm extends EditForm {
    public GenreEditForm(int id) {
        super(new GridLayout(1, 4, 10, 10), id);

        tableName = "gatunek";
        idDbColumnName = "gatunek_id";

        fields.add(new Field("ID gatunku-rodzica", "gatunek_rodzic_id", DataType.INTEGER));
        fields.add(new Field("Nazwa gatunku", "nazwa", DataType.VARCHAR));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}