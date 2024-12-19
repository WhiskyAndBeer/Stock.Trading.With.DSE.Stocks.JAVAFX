package com.example.stocks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBUtility {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/stockmanagement";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    public String u2f(String username) {
        String fullname = null;
        String query = "SELECT fullname FROM userinfo WHERE username = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the parameter for the username
            statement.setString(1, username);

            // Execute the query
            try (ResultSet resultSet = statement.executeQuery()) {
                // If a result is found, retrieve the fullname
                if (resultSet.next()) {
                    fullname = resultSet.getString("fullname");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fullname; // Return the fullname or null if not found
    }
}
