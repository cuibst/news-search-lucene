package com.rzotgorz.service;

import com.rzotgorz.configuration.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class DatabaseConnector {
    // private static final String url = "jdbc:postgresql://localhost:5432/news";

    @Autowired
    private DatabaseConfig config;

    private Connection connection = null;

    private void getConnection() {
        try {
            connection = DriverManager.getConnection(config.url, config.username, config.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet query(String q) {
        if (connection == null) {
            getConnection();
        }
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(q);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return resultSet;
    }

    public boolean modify(String q) {
        if (connection == null) {
            getConnection();
        }
        int result = 0;
        try (Statement statement = connection.createStatement();) {
            result = statement.executeUpdate(q);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return result > 0;
    }
}
