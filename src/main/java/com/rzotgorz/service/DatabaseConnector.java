package com.rzotgorz.service;

import java.sql.*;

public class DatabaseConnector {
    // private static final String url = "jdbc:postgresql://localhost:5432/news";
    private static final String url = "jdbc:postgresql://postgres.rzotgorz.secoder.local:5432/news";
    private static final String username = "postgres";
    private static final String password = "12345678";
    private Connection connection = null;
    private void getConnection() {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
            connection = DriverManager.getConnection(url, username, password);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public ResultSet query(String q) {
        if (connection == null) {
            getConnection();
        }
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(q);) {
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            return null;
        }
        return resultSet;
    }

    public boolean modify(String q) {
        if (connection == null) {
            getConnection();
        }
        int result = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(q);) {
            result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            return false;
        }
        return result > 0;
    }
}
