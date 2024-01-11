package org.example.gui.editform;

import org.example.gui.addform.AddForm;

import javax.swing.*;
import java.awt.*;

public class ItemEditForm extends EditForm {
    public ItemEditForm(int id) {
        super(new GridLayout(1, 2, 10, 10), id);

        tableName = "egzemplarz";
        idDbColumnName = "egzemplarz_id";

        fields.add(new Field("ID książki", "ksiazka_id", DataType.INTEGER));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}