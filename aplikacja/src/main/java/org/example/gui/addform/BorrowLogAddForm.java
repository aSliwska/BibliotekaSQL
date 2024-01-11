package org.example.gui.addform;

import org.example.JDBC;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BorrowLogAddForm extends AddForm implements ItemReturning {
    public BorrowLogAddForm() {
        super(new GridLayout(1, 4, 10, 10));

        tableName = "wypozyczenie";

        fields.add(new Field("ID egzemplarza", "egzemplarz_id", DataType.INTEGER));
        fields.add(new Field("ID czytelnika", "czytelnik_id", DataType.INTEGER));
        specialFields.add(new SpecialField("data_wypozyczenia", SpecialFieldType.CURRENT_TIMESTAMP));
        specialFields.add(new SpecialField("data_zwrotu", SpecialFieldType.NULL));

        for (AddForm.Field field : fields) {
            this.add(new JLabel(field.displayName() + ":", SwingConstants.CENTER));
            this.add(field.textField());
        }
    }

    @Override
    public String returnItem() {
        JDBC db = new JDBC();
        String query = "UPDATE wypozyczenie SET data_zwrotu = NOW()::TIMESTAMP WHERE czytelnik_id = ? AND egzemplarz_id = ? AND data_zwrotu IS NULL";
        int affectedRows = 0;

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Integer.parseInt(fields.get(1).textField().getText()));
            pstmt.setInt(2, Integer.parseInt(fields.get(0).textField().getText()));

            affectedRows = pstmt.executeUpdate();

        } catch (SQLException e) {
            return e.getMessage();
        } catch (NumberFormatException e) {
            return "ID czytelnika i egzemplarza muszą być liczbami całkowitymi.";
        }

        if (affectedRows == 0)
            return "Czytelnik nie ma tego egzemplarza wypożyczonego."; // musi być w kodzie, bo trigger blokowałby zwykłe edytowanie dat (chciałam to udostępnić jako możliwość aplikacji)
        return "Zwrócono książek: " + affectedRows + ".";
    }
}
