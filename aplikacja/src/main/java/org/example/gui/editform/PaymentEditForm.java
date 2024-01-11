package org.example.gui.editform;

import org.example.gui.addform.AddForm;

import javax.swing.*;
import java.awt.*;

public class PaymentEditForm extends EditForm {
    public PaymentEditForm(int id) {
        super(new GridLayout(2, 4, 10, 10), id);

        tableName = "wplata";
        idDbColumnName = "wplata_id";

        fields.add(new Field("ID czytelnika", "czytelnik_id", DataType.INTEGER));
        fields.add(new Field("Kwota", "kwota", DataType.FLOAT));
        fields.add(new Field("Data wpłaty", "data_wplaty", DataType.TIMESTAMP));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }

        this.add(new JLabel("", SwingConstants.CENTER));
        this.add(new JLabel("", SwingConstants.CENTER));
    }
}