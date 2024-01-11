package org.example.gui.editform;

import javax.swing.*;
import java.awt.*;

public class ReaderEditForm extends EditForm {
    public ReaderEditForm(int id) {
        super(new GridLayout(3, 4, 10, 10), id);

        tableName = "czytelnik";
        idDbColumnName = "czytelnik_id";

        fields.add(new Field("Imię", "imie", DataType.VARCHAR));
        fields.add(new Field("Nazwisko", "nazwisko", DataType.VARCHAR));
        fields.add(new Field("Email", "email", DataType.VARCHAR));
        fields.add(new Field("Telefon", "telefon", DataType.VARCHAR));
        fields.add(new Field("Data rejestracji", "data_rejestracji", DataType.DATE));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }

        this.add(new JLabel("", SwingConstants.CENTER));
        this.add(new JLabel("", SwingConstants.CENTER));
    }
}
