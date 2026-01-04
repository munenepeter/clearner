package com.clearner.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String SQLITE_PATH = "./database.sqlite";

    // singleton connection
    public Connection connection = null;

    public Database() {
        connection = init();
    }

    private Connection init() {
        if (connection != null) {
            return connection;
        }

        File file = new File(SQLITE_PATH);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            return DriverManager.getConnection("jdbc:sqlite:" + SQLITE_PATH);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean execute(String sql) {
        try {
            Statement statement = connection.createStatement();
            return statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insert(String table, String columns, String values) {
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", table, columns, values);
        return execute(sql);
    }
}
