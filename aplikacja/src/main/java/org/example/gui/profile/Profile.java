package org.example.gui.profile;

import org.example.JDBC;
import org.example.gui.editform.EditForm;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Function;

abstract public class Profile extends JPanel {
    protected int id;
    protected String nazwaTabeli;

    public Profile(LayoutManager layoutManager, int id, String nazwaTabeli) {
        super(layoutManager);
        this.id = id;
        this.nazwaTabeli = nazwaTabeli;
    }

    protected void constructProfile(String titleText, String nameText, boolean hasTable, JTable table, JPanel stringsPane, JPanel selfRef, EditForm form) {
        // typ profilu
        JLabel title = new JLabel(titleText);
        title.setFont(UIManager.getDefaults().getFont("Label.font").deriveFont(Font.BOLD, 20));

        // edytuj i usun przyciski
        JButton editButton = new JButton("Edytuj");
        JButton deleteButton = new JButton("Usuń");
        JPanel buttonWrapper = new JPanel(new FlowLayout());
        buttonWrapper.add(editButton);
        buttonWrapper.add(deleteButton);

        // wrapper dla naglowka
        JPanel naglowekWrapper = new JPanel(new BorderLayout());
        naglowekWrapper.add(title, BorderLayout.LINE_START);
        naglowekWrapper.add(buttonWrapper, BorderLayout.LINE_END);

        // imie nazwisko
        JLabel name = new JLabel(nameText);
        name.setBorder(BorderFactory.createEmptyBorder(5,15,10,0));
        name.setFont(UIManager.getDefaults().getFont("Label.font").deriveFont(Font.BOLD, 40));

        // gatunki table
        JPanel tableWrapper = null;
        if (hasTable) {
            tableWrapper = new JPanel(new BorderLayout());
            tableWrapper.add(table.getTableHeader(), BorderLayout.PAGE_START);
            tableWrapper.add(table, BorderLayout.CENTER);
        }

        // add everything
        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.PAGE_AXIS));
        leftPane.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        leftPane.add(naglowekWrapper);
        leftPane.add(alignLeft(name));
        leftPane.add(alignLeft(new JLabel("ID: " + String.valueOf(id))));
        leftPane.add(stringsPane);

        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.add(leftPane, BorderLayout.PAGE_START);
        if (hasTable)
            mainPane.add(tableWrapper, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(mainPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        selfRef.add(scrollPane, BorderLayout.CENTER);

        // funkcjonalnosc guzikow
        editButton.addActionListener(e -> {
            selfRef.remove(scrollPane);

            selfRef.add(getEditTab(form), BorderLayout.CENTER);

            selfRef.invalidate();
            selfRef.validate();
            selfRef.repaint();
        });

        deleteButton.addActionListener(e -> {
            delete();
            selfRef.remove(scrollPane);

            JLabel message = new JLabel("Usunięto rekord z ID = " + id);
            message.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

            selfRef.add(message, BorderLayout.PAGE_START);

            selfRef.invalidate();
            selfRef.validate();
            selfRef.repaint();
        });
    }

    protected JScrollPane getEditTab(EditForm form) {
        JPanel tab = new JPanel(new BorderLayout());

        // forma do wprowadzenia danych do edycji
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.add(form, BorderLayout.LINE_START);
        formWrapper.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // guzik
        JButton editButton = new JButton("Edytuj");
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.add(editButton, BorderLayout.LINE_START);
        buttonWrapper.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // powiadomienie zwrotne
        JLabel messageSpot = new JLabel("");
        JPanel messageWrapper = new JPanel(new BorderLayout());
        messageWrapper.add(messageSpot, BorderLayout.PAGE_START);
        messageWrapper.setBorder(BorderFactory.createEmptyBorder(15,15,0,15));

        // dodanie wszystkiego do panelu
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.PAGE_AXIS));
        wrapper.add(formWrapper);
        wrapper.add(buttonWrapper);

        tab.add(wrapper, BorderLayout.PAGE_START);
        tab.add(messageWrapper, BorderLayout.LINE_START);

        // dodanie funkcjonalnosci guzikom
        editButton.addActionListener(e -> {
            messageSpot.setText(form.edit());
            tab.invalidate();
            tab.validate();
        });
        JScrollPane scrollPane = new JScrollPane(tab, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    protected void delete() {
        JDBC db = new JDBC();
        String SQL = "DELETE FROM "+nazwaTabeli+" WHERE "+nazwaTabeli+"_id = ?";

        int affectedrows = 0;

        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, id);

            affectedrows = pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

//        System.out.println("usunieto rekordow: " + affectedrows);
    }

    protected JPanel alignLeft(Container c) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(c, BorderLayout.LINE_START);
        return panel;
    }

    protected abstract void fetchData();

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
