package org.example.gui.addform;

import javax.swing.*;
import java.awt.*;

public class BookAddForm extends AddForm {
    public BookAddForm() {
        super(new GridLayout(2, 4, 10, 10));

        tableName = "ksiazka";

        fields.add(new Field("Tytuł", "tytul", DataType.VARCHAR));
        fields.add(new Field("Opis", "opis", DataType.VARCHAR));
        fields.add(new Field("ID wydawnictwa", "wydawnictwo_id", DataType.INTEGER));
        fields.add(new Field("ID autora", "autor_id", DataType.INTEGER));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}
