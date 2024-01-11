package org.example.gui.editform;

import org.example.JDBC;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class EditForm extends JPanel implements Edition {

    protected String tableName;
    protected enum DataType { VARCHAR, TIMESTAMP, DATE, FLOAT, INTEGER }
    protected record Field(String displayName, String dbName, JTextField textField, DataType dataType) {
        Field(String displayName, String dbName, DataType dataType) {
            this(displayName, dbName, new JTextField(10), dataType);
        }
    }
    protected ArrayList<Field> fields = new ArrayList<Field>();
    protected String idDbColumnName;
    protected int id;

    public EditForm(LayoutManager layoutManager, int id) {
        super(layoutManager);
        this.id = id;
    }

    @Override
    public String edit() {
        JDBC db = new JDBC();

        String query = constructQuery();
//        System.out.println(query);

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            String value;
            for (Field field : fields) {
                value = field.textField.getText();

                if (!value.isEmpty()) {
                    switch (field.dataType) {
                        case VARCHAR -> pstmt.setString(i, value);
                        case TIMESTAMP -> pstmt.setTimestamp(i, Timestamp.valueOf(value));
                        case DATE -> pstmt.setDate(i, Date.valueOf(value));
                        case FLOAT -> pstmt.setFloat(i, Float.parseFloat(value));
                        case INTEGER -> pstmt.setInt(i, Integer.parseInt(value));
                    }
                    i += 1;
                }
            }

            int affectedRows = pstmt.executeUpdate();
        } catch (SQLException e) {
            return e.getMessage();
        } catch (NumberFormatException e) {
            return "W polach liczbowych muszą zawierać się odpowiednio liczby całkowite lub zmiennoprzecinkowe (z kropką).";
        } catch (IllegalArgumentException e) {
            return "Niepoprawny format daty.";
        }

        return "Edytowano obiekt o ID = " + id;
    }

    protected String constructQuery() {
        // UPDATE SET
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(tableName).append(" SET ");

        boolean isFirst = true;

        for (Field field : fields) {
            if (!field.textField.getText().isEmpty()) {
                if (isFirst)
                    isFirst = false;
                else
                    query.append(", ");

                query.append(field.dbName).append(" = ?");
            }
        }

        // WHERE
        query.append(" WHERE ").append(idDbColumnName).append(" = ").append(id);

        return query.toString();
    }
}
