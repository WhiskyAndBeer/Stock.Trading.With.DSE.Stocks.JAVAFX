package com.example.stocks;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class StockDashboardController {

    String ActualUserName;

    @FXML
    private Label username;

    @FXML
    private TableView<Stock> marketTable;

    @FXML
    private TableColumn<Stock, String> colTradingCode;

    @FXML
    private TableColumn<Stock, String> colLastPrice;

    @FXML
    private TableColumn<Stock, String> colChangeValue;

    @FXML
    private TableColumn<Stock, String> colChangePercent;

    private final ObservableList<Stock> stockList = FXCollections.observableArrayList();

    private static final String DB_URL = "jdbc:mysql://localhost:3306/stockmanagement";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    public void setUsername(String fullname) {
        username.setText(fullname);
        loadStockDataFromDatabase(); // Load initial data
        refreshStockDataInBackground(); // Update in background
    }

    public void portFolioLoader(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Portfolio.fxml"));

            loader.setControllerFactory(param -> {
                if (param == StockPortfolioController.class) {
                    StockPortfolioController controller = new StockPortfolioController();
                    controller.setUsername(ActualUserName); // Inject and trigger populateTable
                    return controller;
                } else {
                    try {
                        return param.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Portfolio");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadStockDataFromDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("SELECT * FROM stock_data");

            stockList.clear();
            while (resultSet.next()) {
                stockList.add(new Stock(
                        resultSet.getString("trading_code"),
                        resultSet.getString("last_price"),
                        resultSet.getString("change_value"),
                        resultSet.getString("change_percent")
                ));
            }

            colTradingCode.setCellValueFactory(new PropertyValueFactory<>("tradingCode"));
            colLastPrice.setCellValueFactory(new PropertyValueFactory<>("lastPrice"));
            colChangeValue.setCellValueFactory(new PropertyValueFactory<>("changeValue"));
            colChangePercent.setCellValueFactory(new PropertyValueFactory<>("changePercent"));

            // Custom formatting for change value and percent
            colChangeValue.setCellFactory(column -> new CustomTableCell());
            colChangePercent.setCellFactory(column -> new CustomTableCell());

            marketTable.setItems(stockList);

            // Make the rows clickable
            marketTable.setRowFactory(tv -> {
                TableRow<Stock> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (!row.isEmpty()) {
                        Stock selectedStock = row.getItem();
                        String tradingCode = selectedStock.getTradingCode();
                        String usernameText = username.getText(); // Get the current username displayed
                        System.out.println("Trading Code: " + tradingCode);
                        // Load the StockDetails FXML and pass data
//                        loadStockDetailsScene(usernameText, tradingCode);
                        loadStockDetailsScene(ActualUserName, tradingCode);
                        System.out.println("Statement1");

                    }
                });
                return row;
            });
        } catch (Exception e) {
            System.err.println("Error loading stock data from database: " + e.getMessage());
        }
    }

//    private void refreshStockDataInBackground() {
//        new Thread(() -> {
//            DSEStockScraper.syncDatabaseWithCompanies(); // Sync database
//            Platform.runLater(this::loadStockDataFromDatabase); // Reload table
//        }).start();
//    }
        private void refreshStockDataInBackground() {
            Thread backgroundThread = new Thread(() -> {
                while (true) {
                    try {
                        DSEStockScraper.syncDatabaseWithCompanies(); // Sync database
                        Platform.runLater(this::loadStockDataFromDatabase); // Reload table
                        Thread.sleep(1000); // Pause for 1 second
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
    backgroundThread.setPriority(Thread.MIN_PRIORITY); // Lower the priority
    backgroundThread.start();
}

    // Custom TableCell for conditional formatting
    private static class CustomTableCell extends javafx.scene.control.TableCell<Stock, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("");
            } else {
                setText(item);

                // Apply conditional formatting
                try {
                    double value = Double.parseDouble(item.replace("%", ""));
                    if (value > 0) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (value < 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    }
                } catch (NumberFormatException e) {
                    setStyle("-fx-text-fill: blue; -fx-font-weight: bold;"); // Default for non-numeric values
                }
            }
        }
    }

    // Method to load StockDetails FXML and pass data to the controller
    private void loadStockDetailsScene(String username, String tradingCode) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("StockDetails.fxml"));
            Parent root = loader.load();
            StockDetailsController controller = loader.getController();
            controller.ActualUserName = this.ActualUserName;
            DBUtility dbUtility = new DBUtility();
            controller.initialize(dbUtility.u2f(ActualUserName), tradingCode); // Pass the username and tradingCode
            Stage currentStage = (Stage) marketTable.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
            Parent root = loader.load();
            Stage currentStage =  (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
