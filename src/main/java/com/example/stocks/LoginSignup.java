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

public class LoginSignup {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/stockmanagement";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    // Handle Login Button Action
    @FXML
    private void handleLogin(ActionEvent event) {
        String enteredUsername = username.getText().trim();
        String enteredPassword = password.getText().trim();

        // Validate input
        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            showAlert("Error", "Please enter both username and password.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT fullname FROM userinfo WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, enteredUsername);
                preparedStatement.setString(2, enteredPassword);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String fullname = resultSet.getString("fullname");
                        System.out.println("Welcome, " + fullname);
                        loadDashboard(event, fullname, enteredUsername); // Pass fullname to StockDashboard
                    } else {
                        showAlert("Error", "Invalid username or password.", Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Database connection failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Handle Sign-Up Button Action
    @FXML
    private void switchToSignUp(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("SignUpPage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sign Up");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Unable to load the Sign-Up page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Load Stock Dashboard
    private void loadDashboard(ActionEvent event, String fullname, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StockDashboard.fxml"));
            Parent root = loader.load();

            // Pass the full name to the StockDashboardController
            StockDashboardController controller = loader.getController();
            controller.setUsername(fullname);// Pass username to StockDashboardController
            controller.ActualUserName = username;
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Stock Dashboard");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Unable to load the dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Utility Method to Show Alerts
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
