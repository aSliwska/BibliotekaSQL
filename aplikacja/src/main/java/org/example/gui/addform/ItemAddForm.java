package org.example.gui.addform;

import org.example.JDBC;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class ItemAddForm extends AddForm {
    public ItemAddForm() {
        super(new GridLayout(1, 4, 10, 10));

        tableName = "egzemplarz";

        fields.add(new Field("Ilość egzemplarzy", null, DataType.INTEGER));
        fields.add(new Field("ID książki", "ksiazka_id", DataType.INTEGER));

        for (AddForm.Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }

    // multi insert dla pola ksiazka_id
    @Override
    public String insert() {
        JDBC db = new JDBC();
        int count = 0, amount = 0;

        String query = "INSERT INTO egzemplarz (ksiazka_id) VALUES (?)";
//        System.out.println(query);

        try (Connection conn = db.connect();
             PreparedStatement statement = conn.prepareStatement(query)) {

            amount = Integer.parseInt(fields.get(0).textField().getText());

            for (int i = 0; i < amount; i+=1) {
                statement.setInt(1, Integer.parseInt(fields.get(1).textField().getText()));

                statement.addBatch();
                count++;
                // execute every 100 rows or fewer
                if (count % 100 == 0 || count == amount) {
                    statement.executeBatch();
                }
            }
        } catch (SQLException e) {
            return e.getMessage();
        } catch (NumberFormatException e) {
            return "ID książki i ilość egzemplarzy muszą być liczbami całkowitymi.";
        }

        return "Wstawiono " + count + " nowych egzemplarzy do tabeli.";
    }
}
