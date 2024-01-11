package org.example.gui.editform;

import org.example.gui.addform.AddForm;

import javax.swing.*;
import java.awt.*;

public class PublisherEditForm extends EditForm {
    public PublisherEditForm(int id) {
        super(new GridLayout(1, 4, 10, 10), id);

        tableName = "wydawnictwo";
        idDbColumnName = "wydawnictwo_id";

        fields.add(new Field("Nazwa wydawnictwa", "nazwa", DataType.VARCHAR));
        fields.add(new Field("Data założenia", "data_zalozenia", DataType.DATE));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}