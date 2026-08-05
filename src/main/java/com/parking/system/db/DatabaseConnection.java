package com.parking.system.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection factory. Edit the constants below (or set the
 * matching environment variables) to point at your local MySQL instance.
 */
public final class DatabaseConnection {

    private static final String URL = System.getenv().getOrDefault(
            "PARKING_DB_URL", "jdbc:mysql://localhost:3306/parking_management?useSSL=false&serverTimezone=UTC");
    private static final String USER = System.getenv().getOrDefault("PARKING_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("PARKING_DB_PASSWORD", "");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
