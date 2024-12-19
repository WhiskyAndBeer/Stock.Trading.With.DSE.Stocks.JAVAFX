package com.example.stocks;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;

public class StockDetailsController {

    String ActualUserName;
    @FXML
    private Label changePercent;//

    @FXML
    private Label changeValue;//

    @FXML
    private Label closingPrice;//

    @FXML
    private Label daysRange;//

    @FXML
    private Label lastTradingPrice;//

    @FXML
    private Label lastUpdate;//

    @FXML
    private Label marketCapitalization;

    @FXML
    private Label openingPrice;//

    @FXML
    private Label companyname;

    @FXML
    private Label username;

    @FXML
    private Label weeksMovingRange;//

    @FXML
    private Label yesterdaysClosingPrice;

    @FXML
    private Label tradingCodeLabel;
    @FXML
    private Label adjustedOpeningPrice;
    @FXML
    private Label daysTrade;
    @FXML
    private Label daysVolume;
    @FXML
    private TextField amount;
    @FXML
    private Button buy;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/stockmanagement";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    private String username1;
    private String tradingCode;


    @FXML
    public void handleBuyButtonAction() {
        username1 = username.getText();
        tradingCode = tradingCodeLabel.getText();
        try {
            // Step 1: Load the balance from the database
            System.out.println("Fetching balance for user: " + ActualUserName);
            double balance = getUserBalance(ActualUserName);
            System.out.println("Balance: " + balance);

            // Step 2: Get the entered amount and the last trading price
            double enteredAmount = Double.parseDouble(amount.getText());
            double tradingPrice = Double.parseDouble(lastTradingPrice.getText());

            // Step 3: Calculate the total cost
            double totalCost = enteredAmount * tradingPrice;

            // Step 4: Check if the balance is sufficient
            if (balance >= totalCost) {
                // Step 5: Check if the stock already exists in the user's table
                Double[] existingStock = getExistingStock(ActualUserName, tradingCode);

                if (existingStock != null) {
                    // Update the existing stock record
                    double currentAmount = existingStock[0];
                    double currentAveragePrice = existingStock[1];

                    // Calculate the new amount and average buying price
                    double newAmount = currentAmount + enteredAmount;
                    double newAveragePrice = (currentAmount * currentAveragePrice + enteredAmount * tradingPrice) / newAmount;

                    // Update the database record
                    updateStock(ActualUserName, tradingCode, newAmount, newAveragePrice);
                } else {
                    // Insert a new record if the stock does not exist
                    insertStock(ActualUserName, tradingCode, enteredAmount, tradingPrice);
                }

                // Step 6: Update the balance in the database
                System.out.println("Updating balance for user: " + ActualUserName);
                updateBalance(ActualUserName, balance - totalCost);

                // Show success message
                showAlert("Success", "Purchase Successful", Alert.AlertType.INFORMATION);
            } else {
                // Show insufficient balance alert
                showAlert("Insufficient Balance", "Your balance is insufficient for this purchase.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            // Handle invalid number format
            showAlert("Invalid Input", "Please enter a valid amount.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            // Handle SQL exceptions
            showAlert("Database Error", "There was an error with the database operation.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    public void handleSellButtonAction() {
        username1 = username.getText();
        tradingCode = tradingCodeLabel.getText();

        try {
            // Step 1: Get the amount to sell and the current price of the stock
            double sellAmount = Double.parseDouble(amount.getText());
            double tradingPrice = Double.parseDouble(lastTradingPrice.getText());

            // Step 2: Check if the stock exists and get the current amount
            Double[] existingStock = getExistingStock(username1, tradingCode);

            if (existingStock != null) {
                double currentAmount = existingStock[0];
                double totalSaleValue = sellAmount * tradingPrice;

                if (sellAmount <= currentAmount) {
                    // Step 3: Update the stock or delete the row if the remaining amount is zero
                    double newAmount = currentAmount - sellAmount;

                    if (newAmount > 0) {
                        updateStock(username1, tradingCode, newAmount, existingStock[1]); // Keep the same average price
                    } else {
                        deleteStock(username1, tradingCode); // Delete the stock row
                    }

                    // Step 4: Update the user's balance
                    double currentBalance = getUserBalance(username1);
                    updateBalance(username1, currentBalance + totalSaleValue);

                    // Show success message
                    showAlert("Success", "Stock sold successfully!", Alert.AlertType.INFORMATION);
                } else {
                    // Show insufficient stock alert
                    showAlert("Insufficient Stock", "You do not have enough of this stock to sell.", Alert.AlertType.ERROR);
                }
            } else {
                // Show stock not found alert
                showAlert("Stock Not Found", "You do not own this stock.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            // Handle invalid number format
            showAlert("Invalid Input", "Please enter a valid amount.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            // Handle SQL exceptions
            showAlert("Database Error", "There was an error with the database operation.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    private void deleteStock(String username, String tradingCode) throws SQLException {
        String query = "DELETE FROM stocks_" + username + " WHERE trading_code = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tradingCode);
            stmt.executeUpdate();
            System.out.println("Stock deleted for user: " + username + ", trading code: " + tradingCode);
        }
    }



    private double getUserBalance(String username) throws SQLException {
        String balanceString = null; // To hold the balance as a String
        double balance = 0.0; // To hold the balance as a double
        System.out.println("Trying to fetch user balance for username: " + username);

        String query = "SELECT balance FROM userinfo WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username); // Set the username in the prepared statement
            System.out.println("Executing query: " + stmt);

            ResultSet rs = stmt.executeQuery();

            // Check if we have a result and retrieve the balance
            if (rs.next()) {
                balanceString = rs.getString("balance");
                System.out.println("Balance fetched as string: " + balanceString);

                // Convert the String to double
                if (balanceString != null) {
                    balance = Double.parseDouble(balanceString);
                }
            } else {
                System.out.println("No user found with username: " + username);
            }
        } catch (SQLException e) {
            // Handle any SQL exceptions
            System.err.println("Error fetching balance: " + e.getMessage());
            throw e; // Rethrow the exception to be handled elsewhere
        } catch (NumberFormatException e) {
            // Handle potential format issues during String to double conversion
            System.err.println("Error parsing balance to double: " + e.getMessage());
            throw e; // Rethrow or handle as necessary
        }

        System.out.println("Balance: " + balance);
        return balance;
    }

    private void updateBalance(String username, double newBalance) throws SQLException {
        System.out.println("Updating balance for user: " + username);
        String query = "UPDATE userinfo SET balance = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, username);
            stmt.executeUpdate();
            System.out.println("Balance updated successfully for user: " + username);
        }
    }
    // Check if stock exists and get current amount and average price
    private Double[] getExistingStock(String username, String tradingCode) throws SQLException {
        String query = "SELECT amount, purchasing_price FROM stocks_" + username + " WHERE trading_code = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tradingCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Double[]{rs.getDouble("amount"), rs.getDouble("purchasing_price")};
                }
            }
        }
        return null; // Stock does not exist
    }

    // Update existing stock
    private void updateStock(String username, String tradingCode, double newAmount, double newAveragePrice) throws SQLException {
        String query = "UPDATE stocks_" + username + " SET amount = ?, purchasing_price = ? WHERE trading_code = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newAmount);
            stmt.setDouble(2, newAveragePrice);
            stmt.setString(3, tradingCode);
            stmt.executeUpdate();
        }
    }

    // Insert the stock details into the user's stocks table
    private void insertStock(String username, String tradingCode, double amount, double purchasePrice) throws SQLException {
        String query = "INSERT INTO stocks_" + username + " (trading_code, amount, purchasing_price, purchase_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tradingCode);
            stmt.setDouble(2, amount);
            stmt.setDouble(3, purchasePrice);
            stmt.setDate(4, new java.sql.Date(System.currentTimeMillis())); // Current date
            stmt.executeUpdate();
        }
    }

    // Initialize method to accept username and trading code
    public void initialize(String username1, String tradingCode) {
        username.setText(username1);
        tradingCodeLabel.setText(tradingCode);

        // Fetch stock details using the trading code
        fetchStockDetails(tradingCode);
    }

    // Method to fetch stock details based on the trading code
    private void fetchStockDetails(String tradingCode) {
        String url = "https://www.dsebd.org/displayCompany.php?name=" + tradingCode;

        try {
            // Connect to the website and get the HTML document
            Document doc = Jsoup.connect(url).get();
            String companyName = new String();

            Element companyNameElement = doc.selectFirst("h2.BodyHead.topBodyHead i");

            if (companyNameElement != null) {
                // Extract and print the company name
                companyName = companyNameElement.text();
                System.out.println("Company Name: " + companyName);
            } else {
                System.out.println("Could not find the company name on the page.");
            }
            companyname.setText(companyName);
            // Renamed variables to avoid overlap with label names
            String fetchedLastTradingPrice = null;//
            String fetchedClosingPrice = null;//
            String fetchedLastUpdate = null;//
            String fetchedDaysRange = null;//
            String fetchedChangeValue = null;//
            String fetchedChangePercent = null;//
            String fetchedOpeningPrice = null;//
            String fetchedWeeksMovingRange = null;//
            String fetchedYesterdaysClosingPrice = null;
            String fetchedAdjustedOpeningPrice = null;
            String fetchedDaysVolume = null;
            String fetchedDaysTrade = null;
            String fetchedMarketCapitalization = null;

            // Select all rows in the table
            Elements rows = doc.select("tr");

            // Iterate through the rows to extract the data
            for (Element row : rows) {
                Elements header = row.select("th");
                Elements data = row.select("td");

                if (header.size() > 0 && data.size() > 0) {
                    String headerText = header.get(0).text();
                    switch (headerText) {
                        case "Last Trading Price":
                            fetchedLastTradingPrice = data.get(0).text();
                            fetchedClosingPrice = data.get(1).text();
                            break;
                        case "Last Update":
                            fetchedLastUpdate = data.get(0).text();
                            fetchedDaysRange = data.get(1).text();
                            break;
                        case "Change*":
                            fetchedChangeValue = data.get(0).text();
                            fetchedChangePercent = rows.get(rows.indexOf(row) + 1).select("td").get(0).text();
                            break;
                        case "52 Weeks' Moving Range":
                            fetchedWeeksMovingRange = data.get(1).text();
                            break;
                        case "Opening Price":
                            fetchedOpeningPrice = data.get(0).text();
                            fetchedDaysVolume = data.get(1).text();
                            break;
                        case "Adjusted Opening Price":
                            fetchedAdjustedOpeningPrice = data.get(0).text();
                            fetchedDaysTrade = data.get(1).text();
                            break;
                        case "Yesterday's Closing Price":
                            fetchedYesterdaysClosingPrice = data.get(0).text();
                            fetchedMarketCapitalization = data.get(1).text();
                            break;
                    }
                }
            }

            // Update the labels with the fetched data
            lastTradingPrice.setText(fetchedLastTradingPrice);
            closingPrice.setText(fetchedClosingPrice);
            lastUpdate.setText(fetchedLastUpdate);
            daysRange.setText(fetchedDaysRange);
            changeValue.setText(fetchedChangeValue);
            changePercent.setText(fetchedChangePercent);
            openingPrice.setText(fetchedOpeningPrice);
            weeksMovingRange.setText(fetchedWeeksMovingRange);
            yesterdaysClosingPrice.setText(fetchedYesterdaysClosingPrice);
            marketCapitalization.setText(fetchedMarketCapitalization);
            adjustedOpeningPrice.setText(fetchedAdjustedOpeningPrice);
            daysTrade.setText(fetchedDaysTrade);
            daysVolume.setText(fetchedDaysVolume);

//            String fetchedAdjustedOpeningPrice = null;
//            String fetchedDaysVolume = null;
//            String fetchedDaysTrade = null;


        } catch (Exception e) {
            System.err.println("Error fetching stock data: " + e.getMessage());
        }
    }
    public void back(ActionEvent event) {
        loadDashboard(event, ActualUserName);//username.getText());
    }

    private void loadDashboard(ActionEvent event, String fullname) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StockDashboard.fxml"));
            Parent root = loader.load();

            // Pass the full name to the StockDashboardController
            StockDashboardController controller = loader.getController();
            DBUtility dbUtility = new DBUtility();
            controller.setUsername(dbUtility.u2f(fullname));
            controller.ActualUserName = ActualUserName;
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
