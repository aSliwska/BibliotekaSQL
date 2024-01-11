package org.example;

import java.sql.*;

public class JDBC {
    private final String url = "********";
    private final String user = "********";
    private final String password = "********";

    public Connection connect() {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url, user, password);
//            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
