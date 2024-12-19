package com.example.stocks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockPortfolioController {

    public String username; // Ensure this is set before calling populateTable
    private static final String DB_URL = "jdbc:mysql://localhost:3306/stockmanagement";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    @FXML
    private Label FullName;

    @FXML
    private TableView<StockData> portfolioTable;

    @FXML
    private TableColumn<StockData, String> tradingCodeColumn;

    @FXML
    private TableColumn<StockData, Double> purchasePriceColumn;

    @FXML
    private TableColumn<StockData, Double> currentPriceColumn;

    @FXML
    private TableColumn<StockData, Integer> quantityColumn;

    @FXML
    private TableColumn<StockData, Double> totalValueColumn;

    @FXML
    private TableColumn<StockData, Double> unauthorizedGainColumn;

    @FXML
    private TableColumn<StockData, Double> unauthorizedGainPercentageColumn;

    @FXML
    private Label totalValue;

    @FXML
    private Label Balance;

    @FXML
    private Label totalDeposit;

    @FXML
    private Label totalWithdraw;

    @FXML
    private Label netGain;

    @FXML
    private Label netGainPercentage;
    public void setUsername(String username) {
        this.username = username;
    }
    @FXML
    public void initializeLabels(String username) {
        try {
            // Step 1: Fetch user information from `userinfo` table
            String userInfoQuery = "SELECT balance, total_deposit, withdraw FROM userinfo WHERE username = ?";
            double balance = 0.0, totalDeposit = 0.0, totalWithdraw = 0.0;

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(userInfoQuery)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        balance = rs.getDouble("balance");
                        totalDeposit = rs.getDouble("total_deposit");
                        totalWithdraw = rs.getDouble("withdraw");
                    }
                }
            }

            // Step 2: Initialize scraper and calculate total value and total investment
            String stockQuery = "SELECT trading_code, purchasing_price, amount FROM stocks_" + username;
            double totalValue = 0.0;  // Current total value of all stocks
            double totalInvestment = 0.0;  // Total invested (amount × purchasing_price)

            DSEStockScraper dt = new DSEStockScraper(); // Initialize the scraper

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(stockQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tradingCode = rs.getString("trading_code");
                    double purchasingPrice = rs.getDouble("purchasing_price");
                    double amount = rs.getDouble("amount");

                    // Fetch the current price using the scraper
                    double currentPrice = dt.CurrentPrice(tradingCode);

                    // Update total investment and total value
                    totalInvestment += purchasingPrice * amount; // Original investment
                    totalValue += currentPrice * amount; // Current value
                }
            }

            // Step 3: Calculate net gain and percentage
            double netGain = totalValue - totalInvestment;  // Absolute net gain
            double netGainPercentage = totalInvestment > 0 ? (totalValue / totalInvestment - 1) * 100 : 0.0;
            System.out.println("Net gain: " + netGain);
            // Step 4: Update the labels
            this.Balance.setText(String.format("%.2f", balance));
            this.totalDeposit.setText(String.format("%.2f", totalDeposit));
            this.totalWithdraw.setText(String.format("%.2f", totalWithdraw));
            this.totalValue.setText(String.format("%.2f", totalValue));
            this.netGain.setText(String.format("%.2f", netGain));
            this.netGainPercentage.setText(String.format("%.2f%%", netGainPercentage));

        } catch (SQLException e) {
            // Handle SQL exceptions
            showAlert("Database Error", "An error occurred while fetching data from the database.", Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            // Handle any unexpected exceptions (e.g., network issues with scraper)
            showAlert("Error", "An error occurred while fetching stock prices.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {

        tradingCodeColumn.setCellValueFactory(new PropertyValueFactory<>("tradingCode"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalValueColumn.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        unauthorizedGainColumn.setCellValueFactory(new PropertyValueFactory<>("unauthorizedGain"));
        unauthorizedGainPercentageColumn.setCellValueFactory(new PropertyValueFactory<>("unauthorizedGainPercentage"));
        System.out.println("Columns bound to fields in StockData");
        List<StockData> stockDataList = new ArrayList<>();
        if (username == null || username.isEmpty()) {
            System.err.println("Username is not set. Cannot fetch data.");
            return;
        }
        String tableName = "stocks_" + username;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Query to fetch trading code, purchase price, and quantity
            String query = "SELECT trading_code, purchasing_price, amount FROM " + tableName;
            System.out.println(query);
            PreparedStatement statement = connection.prepareStatement(query);
            System.out.println("Statement created");
            ResultSet resultSet = statement.executeQuery();
            System.out.println("Query executed");
            int n = 0;
            stockDataList.clear(); // Clear the list before adding new data
            while (resultSet.next()) {
                String tradingCode = resultSet.getString("trading_code");
                double purchasePrice = resultSet.getDouble("purchasing_price");
                int quantity = resultSet.getInt("amount");
                DSEStockScraper scraper = new DSEStockScraper();
                double currentPrice = scraper.CurrentPrice(tradingCode);
                double totalValue = currentPrice * quantity;
                double unauthorizedGain = totalValue - (purchasePrice * quantity);
                double unauthorizedGainPercentage = (unauthorizedGain / (purchasePrice * quantity)) * 100;
                StockData stockData = new StockData(tradingCode, purchasePrice, currentPrice, quantity, totalValue, unauthorizedGain, unauthorizedGainPercentage);
                stockDataList.add(stockData);
                stockData.printStockData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ObservableList<StockData> data = FXCollections.observableArrayList(stockDataList);
        portfolioTable.setItems(data);


        portfolioTable.setRowFactory(tv -> {
            TableRow<StockData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    StockData selectedStock = row.getItem();
                    String tradingCode = selectedStock.getTradingCode();
                    loadStockDetailsScene(username, tradingCode);
                }
            });
            return row;
        });
        DBUtility dbUtility = new DBUtility();
        String fullName = dbUtility.u2f(username);
        System.out.println("Full name: " + fullName);
        FullName.setText(fullName);
        initializeLabels(username);
    }
    public void back(ActionEvent event) {
        loadDashboard(event, username);//username.getText());
    }

    private void loadDashboard(ActionEvent event, String fullname) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StockDashboard.fxml"));
            Parent root = loader.load();

            // Pass the full name to the StockDashboardController
            StockDashboardController controller = loader.getController();
            DBUtility dbUtility = new DBUtility();
            controller.setUsername(dbUtility.u2f(fullname));
            controller.ActualUserName = username;
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Stock Dashboard");
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Unable to load the dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void loadStockDetailsScene(String username, String tradingCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StockDetails.fxml"));
            Parent root = loader.load();

            StockDetailsController controller = loader.getController();
            controller.initialize(username, tradingCode); // Pass the required data
            controller.ActualUserName = username;
            DBUtility dbUtility = new DBUtility();
            controller.initialize(dbUtility.u2f(username), tradingCode);
            Stage currentStage = (Stage) portfolioTable.getScene().getWindow();
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
