package org.example.gui.searchform;

import javax.swing.*;
import java.awt.*;

public class PublisherSearchForm extends SearchForm {
    public PublisherSearchForm() {
        super(new GridLayout(1, 6, 10, 10));

        profileType = ProfileType.PUBLISHER;

        mainTable = new TableName("wydawnictwo", "w");

        orderByField = new Field("ID wydawnictwa", "wydawnictwo_id", DataType.INTEGER, mainTable);

        fields.add(orderByField);
        fields.add(new Field("Nazwa wydawnictwa", "nazwa", DataType.VARCHAR, mainTable));
        fields.add(new Field("Data założenia", "data_zalozenia", DataType.DATE, mainTable));

        for (Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }
}