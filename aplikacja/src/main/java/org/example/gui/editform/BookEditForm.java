package org.example.gui.editform;

import org.example.gui.addform.AddForm;

import javax.swing.*;
import java.awt.*;

public class BookEditForm extends EditForm {
    public BookEditForm(int id) {
        super(new GridLayout(2, 4, 10, 10), id);

        tableName = "ksiazka";
        idDbColumnName = "ksiazka_id";

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