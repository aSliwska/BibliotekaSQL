package org.example.gui.addform;

import org.example.JDBC;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Collectors;

abstract public class AddForm extends JPanel implements Addition {

    protected String tableName;
    protected enum DataType { VARCHAR, TIMESTAMP, DATE, FLOAT, INTEGER }
    protected enum SpecialFieldType { CURRENT_TIMESTAMP, CURRENT_DATE, NULL }
    protected record SpecialField(String dbName, SpecialFieldType fieldType) {}
    protected record Field(String displayName, String dbName, JTextField textField, DataType dataType) {
        Field(String displayName, String dbName, DataType dataType) {
            this(displayName, dbName, new JTextField(10), dataType);
        }
    }
    protected ArrayList<Field> fields = new ArrayList<Field>();
    protected ArrayList<SpecialField> specialFields = new ArrayList<SpecialField>();

    public AddForm(LayoutManager layoutManager) {
        super(layoutManager);
    }

    public void clearFields() {
        for (Field field : fields)
            field.textField.setText("");
    }

    @Override
    public String insert() {

        for (Field field : fields)
            if (field.textField.getText().isEmpty())
                return "Wszystkie pola muszą być zapełnione.";

        JDBC db = new JDBC();
        long id = 0;

        String query = constructQuery();
//        System.out.println(query);

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            String value;
            for (Field field : fields) {
                value = field.textField.getText();

                switch (field.dataType) {
                    case VARCHAR -> pstmt.setString(i, value);
                    case TIMESTAMP -> pstmt.setTimestamp(i, Timestamp.valueOf(value));
                    case DATE -> pstmt.setDate(i, Date.valueOf(value));
                    case FLOAT -> pstmt.setFloat(i, Float.parseFloat(value));
                    case INTEGER -> pstmt.setInt(i, Integer.parseInt(value));
                }
                i += 1;
            }

            int affectedRows = pstmt.executeUpdate();
            // check the affected rows
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getLong(1);
                    }
                } catch (SQLException e) {
                    return e.getMessage();
                }
            }
        } catch (SQLException e) {
            return e.getMessage();
        } catch (NumberFormatException e) {
            return "W polach liczbowych muszą zawierać się odpowiednio liczby całkowite lub zmiennoprzecinkowe (z kropką).";
        } catch (IllegalArgumentException e) {
            return "Niepoprawny format daty.";
        }

        if (tableName.equals("gatunek_ksiazki"))
            return "Wstawiono do tabeli.";
        return "Wstawiono do tabeli. Przypisane ID = " + id + ".";
    }

    protected String constructQuery() {
        // INSERT
        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(tableName).append(" (");

        boolean isFirst = true;

        for (Field field : fields) {
            if (isFirst)
                isFirst = false;
            else
                query.append(", ");

            query.append(field.dbName);
        }
        for (SpecialField specialField : specialFields) {
            if (isFirst)
                isFirst = false;
            else
                query.append(", ");

            query.append(specialField.dbName);
        }

        // VALUES
        query.append(") VALUES (");

        isFirst = true;

        for (Field field : fields) {
            if (isFirst)
                isFirst = false;
            else
                query.append(", ");
            query.append("?");
        }
        for (SpecialField specialField : specialFields) {
            if (isFirst)
                isFirst = false;
            else
                query.append(", ");

            switch (specialField.fieldType) {
                case NULL -> query.append("NULL");
                case CURRENT_DATE -> query.append("NOW()::DATE");
                case CURRENT_TIMESTAMP -> query.append("NOW()::TIMESTAMP");
            }
        }

        query.append(")");

        return query.toString();
    }
}
