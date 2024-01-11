package org.example.gui.addform;

import javax.swing.*;
import java.awt.*;

public class PublisherAddForm extends AddForm {
    public PublisherAddForm() {
        super(new GridLayout(1, 4, 10, 10));

        tableName = "wydawnictwo";

        fields.add(new Field("Nazwa wydawnictwa", "nazwa", DataType.VARCHAR));
        fields.add(new Field("Data założenia", "data_zalozenia", DataType.DATE));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}