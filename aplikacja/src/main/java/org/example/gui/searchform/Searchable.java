package org.example.gui.searchform;

import javax.swing.*;

public interface Searchable {
    enum DataType { VARCHAR, TIMESTAMP, DATE, FLOAT, INTEGER }
    JTable search(JLabel errorMessage);
}
