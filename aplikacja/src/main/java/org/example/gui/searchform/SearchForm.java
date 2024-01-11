package org.example.gui.searchform;

import org.example.JDBC;
import org.example.gui.profile.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

abstract public class SearchForm extends JPanel implements Searchable {

    protected record TableName(String longName, String shortName, String joinOn, boolean isLeftJoinable) {
        TableName(String longName, String shortName, String joinOn) {
            this(longName, shortName, joinOn, false);
        }

        TableName(String longName, String shortName) {
            this(longName, shortName, null);
        }
    }
    protected TableName mainTable;
    protected ArrayList<TableName> additionalJoinTables = new ArrayList<TableName>();

    protected record Field(String displayName, String dbName, JTextField textField, DataType dataType, TableName table) {
        Field(String displayName, String dbName, DataType dataType, TableName table) {
            this(displayName, dbName, new JTextField(10), dataType, table);
        }
    }
    protected ArrayList<Field> fields = new ArrayList<Field>();
    protected Field orderByField;

    protected enum ProfileType { AUTHOR, BOOK, GENRE, PUBLISHER, READER, BORROW_LOG, PAYMENT, ITEM, GENRE_TO_BOOK}
    protected ProfileType profileType;

    protected ArrayList<Field> additionalDisplayColumns = new ArrayList<Field>();

    public SearchForm(LayoutManager layoutManager) {
        super(layoutManager);
    }

    public void clearFields() {
        for (Field field : fields)
            field.textField.setText("");
    }

    @Override
    public JTable search(JLabel errorMessage) {
        JDBC db = new JDBC();

        ArrayList<String> columnNames = new ArrayList<String>();
        for (Field field : fields) {
            if (field.table.equals(mainTable)) {
                columnNames.add(field.displayName());
            }
        }
        for (Field field : additionalDisplayColumns) {
            columnNames.add(field.displayName());
        }
        Object[][] rowData = {};

        String query = constructQuery();
//        System.out.println(query);

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            int i = 1;
            String value;
            for (Field field : fields) {
                if (!field.textField.getText().isEmpty()) {
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
            }

            ResultSet rs = pstmt.executeQuery();
            rowData = formatToRows(rs);
            errorMessage.setText("");

        } catch (SQLException e) {
            errorMessage.setText(e.getMessage());
        } catch (NumberFormatException e) {
            errorMessage.setText("W polach liczbowych muszą zawierać się odpowiednio liczby całkowite lub zmiennoprzecinkowe (z kropką).");
        } catch (IllegalArgumentException e) {
            errorMessage.setText("Niepoprawny format daty.");
        }

//        System.out.println(Arrays.deepToString(rowData));

        // create and return table
        JTable table = new JTable(createTableModel(columnNames.toArray(new String[0]), rowData));


        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);

                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    JFrame frame = new JFrame("Biblioteka SQL - profil elementu");
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    int id, gatunek_id = 0;
                    try {
                        id = Integer.parseInt(table.getValueAt(row, 0).toString());

                        if (profileType.equals(ProfileType.GENRE_TO_BOOK))
                            gatunek_id = Integer.parseInt(table.getValueAt(row, 1).toString());

                    } catch (NumberFormatException e) {
                        frame.dispose();
                        e.printStackTrace();
                        return;
                    }

                    switch (profileType) {
                        case AUTHOR -> frame.setContentPane(new AuthorProfile(id));
                        case BOOK -> frame.setContentPane(new BookProfile(id));
                        case GENRE -> frame.setContentPane(new GenreProfile(id));
                        case PUBLISHER -> frame.setContentPane(new PublisherProfile(id));
                        case READER -> frame.setContentPane(new ReaderProfile(id));
                        case BORROW_LOG -> frame.setContentPane(new BorrowLogProfile(id));
                        case PAYMENT -> frame.setContentPane(new PaymentProfile(id));
                        case ITEM -> frame.setContentPane(new ItemProfile(id));
                        case GENRE_TO_BOOK -> frame.setContentPane(new GenreToBookProfile(id, gatunek_id));
                    }

                    frame.setMinimumSize(new Dimension(700, 400));
                    frame.setSize(700, 500);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            }
        });


        return table;
    }

    protected String constructQuery() {
        // SELECT
        StringBuilder query = new StringBuilder("SELECT ");

        // kolumny do wyswietlenia
        StringBuilder selectedFields = new StringBuilder();
        boolean isFirst = true;

        for (Field field : fields) {
            if (field.table().equals(mainTable)) {
                if (isFirst)
                    isFirst = false;
                else
                    selectedFields.append(", ");

                selectedFields.append(field.table().shortName()).append(".").append(field.dbName());
            }
        }
        for (Field field : additionalDisplayColumns) {
            if (isFirst)
                isFirst = false;
            else
                selectedFields.append(", ");

            selectedFields.append(field.table().shortName()).append(".").append(field.dbName());
        }
        query.append(selectedFields);

        // FROM nazwa_tabeli skrot
        boolean leftJoined = false;
        query.append(" FROM ").append(mainTable.longName()).append(" ").append(mainTable.shortName());

        if (!additionalJoinTables.isEmpty()) {
            // JOIN
            for (TableName additionalTable : additionalJoinTables) {
                if (!additionalTable.isLeftJoinable)
                    query.append(" JOIN ").append(additionalTable.longName()).append(" ").append(additionalTable.shortName()).append(" USING (").append(additionalTable.joinOn()).append(")");
            }
            // LEFT JOIN
            for (TableName additionalTable : additionalJoinTables) {
                if (additionalTable.isLeftJoinable) {
                    query.append(" LEFT JOIN ").append(additionalTable.longName()).append(" ").append(additionalTable.shortName()).append(" USING (").append(additionalTable.joinOn()).append(")");
                    leftJoined = true;
                }
            }
        }

        // WHERE
        isFirst = true;

        for (Field field : fields) {
            if (!field.textField().getText().isEmpty()) {
                if (isFirst) {
                    query.append(" WHERE ");
                    isFirst = false;
                }
                else
                    query.append(" AND ");

                query.append(field.table().shortName()).append(".").append(field.dbName());

                if (field.dataType().equals(DataType.VARCHAR))
                    query.append(" LIKE ");
                else
                    query.append(" = ");

                query.append("?");
            }
        }

        // GROUP BY
        if (leftJoined)
            query.append(" GROUP BY ").append(selectedFields);

        // ORDER BY
        query.append(" ORDER BY ").append(orderByField.table().shortName()).append(".").append(orderByField.dbName());

        return query.toString();
    }

    protected Object[][] formatToRows(ResultSet rs) throws SQLException {
        ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();

        while (rs.next()) {
            ArrayList<Object> row = new ArrayList<Object>();
            for (Field field : fields) {
                if (field.table().equals(mainTable)) {
                    row.add(rs.getString(field.dbName()));
                }
            }
            for (Field field : additionalDisplayColumns) {
                row.add(rs.getString(field.dbName()));
            }

            result.add(row);
        }

        // convert 2d array list to 2d array
        return result.stream().map(u -> u.toArray(new Object[0])).toArray(Object[][]::new);
    }

    protected TableModel createTableModel(String[] columnNames, Object[][] rowData) {
        return new AbstractTableModel() {
            private String[] columns = columnNames;
            private Object[][] data = rowData;

            @Override
            public int getRowCount() {
                return data.length;
            }

            @Override
            public int getColumnCount() {
                return columns.length;
            }

            @Override
            public String getColumnName(int columnIndex) {
                return columns[columnIndex];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return data[rowIndex][columnIndex];
            }
        };
    }
}
