package org.example.gui.addform;

import javax.swing.*;
import java.awt.*;

public class PaymentAddForm extends AddForm {
    public PaymentAddForm() {
        super(new GridLayout(1, 4, 10, 10));

        tableName = "wplata";

        fields.add(new Field("ID czytelnika", "czytelnik_id", DataType.INTEGER));
        fields.add(new Field("Kwota", "kwota", DataType.FLOAT));
        specialFields.add(new SpecialField("data_wplaty", SpecialFieldType.CURRENT_TIMESTAMP));

        for (AddForm.Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}
