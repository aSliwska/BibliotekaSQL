package org.example.gui.addform;

import javax.swing.*;
import java.awt.*;

public class ReaderAddForm extends AddForm {
    public ReaderAddForm() {
        super(new GridLayout(2, 4, 10, 10));

        tableName = "czytelnik";

        fields.add(new Field("Imię", "imie", DataType.VARCHAR));
        fields.add(new Field("Nazwisko", "nazwisko", DataType.VARCHAR));
        fields.add(new Field("Email", "email", DataType.VARCHAR));
        fields.add(new Field("Telefon", "telefon", DataType.VARCHAR));
        specialFields.add(new SpecialField("data_rejestracji", SpecialFieldType.CURRENT_DATE));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}
