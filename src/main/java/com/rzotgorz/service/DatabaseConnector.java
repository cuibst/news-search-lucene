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

    public ResultSet query(String q) throws SQLException{
        PreparedStatement preparedStatement = null;
        if (connection == null) {
            getConnection();
        }
        ResultSet resultSet = null;
        preparedStatement = connection.prepareStatement(q);
        resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public boolean modify(String q) throws SQLException{
        PreparedStatement preparedStatement = null;
        if (connection == null) {
            getConnection();
        }
        int result = 0;
        preparedStatement = connection.prepareStatement(q);
        result = preparedStatement.executeUpdate();
        return result > 0;
    }
}
