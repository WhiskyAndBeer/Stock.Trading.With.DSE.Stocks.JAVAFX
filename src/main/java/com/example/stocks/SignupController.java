package com.example.stocks;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignupController {

    @FXML
    private TextField name;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private TextField mobileNo;

    @FXML
    private TextField depositedAmount;

    // MySQL connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/stockmanagement";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    @FXML
    private void handleSignup() {
        String fullName = name.getText().trim();
        String userName = username.getText().trim();
        String userPassword = password.getText().trim();
        String mobileNumber = mobileNo.getText().trim();
        String balance = depositedAmount.getText().trim();

        // Validate inputs
        if (fullName.isEmpty() || userName.isEmpty() || userPassword.isEmpty() || mobileNumber.isEmpty() || balance.isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        try {
            int mobile = Integer.parseInt(mobileNumber);
            int depositedBalance = Integer.parseInt(balance);

            // Connect to the database
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Check if the username already exists
                if (isUsernameTaken(connection, userName)) {
                    showAlert("Error", "Username already exists. Please choose a different username.", Alert.AlertType.ERROR);
                    return;
                }

                // Insert new user data into the database
                String insertQuery = "INSERT INTO userinfo (fullname, username, password, mobile, balance, total_deposit) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, fullName);
                    preparedStatement.setString(2, userName);
                    preparedStatement.setString(3, userPassword);
                    preparedStatement.setInt(4, mobile);
                    preparedStatement.setInt(5, depositedBalance);
                    preparedStatement.setInt(6, depositedBalance);

                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        // Create a stock table for the user
                        createStockTableForUser(connection, userName);
                        showAlert("Success", "Sign-Up successful!", Alert.AlertType.INFORMATION);
                        clearFields();
                    } else {
                        showAlert("Error", "An error occurred. Please try again.", Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid mobile number or balance. Please enter numeric values.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Error", "Database connection failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createStockTableForUser(Connection connection, String username) throws SQLException {
        // Replace any special characters in the username to ensure valid table names
        String sanitizedUsername = username.replaceAll("[^a-zA-Z0-9]", "_");

        // Dynamically create a table for the user
        String createTableQuery = "CREATE TABLE IF NOT EXISTS stocks_" + sanitizedUsername + " (" +
                "id INT NOT NULL AUTO_INCREMENT," +
                "trading_code VARCHAR(45) NOT NULL," +
                "amount INT NOT NULL," +
                "purchasing_price DECIMAL(10, 2) NOT NULL," +
                "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (id))";

        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            preparedStatement.executeUpdate();
        }
    }

    private boolean isUsernameTaken(Connection connection, String username) throws SQLException {
        String query = "SELECT username FROM userinfo WHERE username = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // If a result exists, username is taken
            }
        }
    }

    private void clearFields() {
        name.clear();
        username.clear();
        password.clear();
        mobileNo.clear();
        depositedAmount.clear();
    }
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Load the FXMLDocument.fxml file
            Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to load the main screen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
