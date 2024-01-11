package org.example.gui.editform;

import org.example.gui.addform.AddForm;

import javax.swing.*;
import java.awt.*;

public class BorrowLogEditForm extends EditForm {
    public BorrowLogEditForm(int id) {
        super(new GridLayout(2, 4, 10, 10), id);

        tableName = "wypozyczenie";
        idDbColumnName = "wypozyczenie_id";

        fields.add(new Field("ID egzemplarza", "egzemplarz_id", DataType.INTEGER));
        fields.add(new Field("ID czytelnika", "czytelnik_id", DataType.INTEGER));
        fields.add(new Field("Data wypożyczenia", "data_wypozyczenia", DataType.TIMESTAMP));
        fields.add(new Field("Data zwrotu", "data_zwrotu", DataType.TIMESTAMP));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}