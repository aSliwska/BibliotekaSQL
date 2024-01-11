package org.example.gui.report;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

abstract public class Report extends JPanel implements Reportable {

    abstract public JTable fetch();

    protected Object[][] formatToRows(ResultSet rs, String[] dbNames) throws SQLException {
        ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();

        while (rs.next()) {
            ArrayList<Object> row = new ArrayList<Object>();
            for (String name : dbNames)
                row.add(rs.getString(name));

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
