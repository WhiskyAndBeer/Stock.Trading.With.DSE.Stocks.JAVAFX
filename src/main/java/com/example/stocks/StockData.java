package com.example.stocks;


public class StockData {

    private String tradingCode;
    private double purchasePrice;
    private double currentPrice;
    private int quantity;
    private double totalValue;
    private double unauthorizedGain;
    private double unauthorizedGainPercentage;

    public StockData(String tradingCode, double purchasePrice, double currentPrice, int quantity, double totalValue, double unauthorizedGain, double unauthorizedGainPercentage) {
        this.tradingCode = tradingCode;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.unauthorizedGain = unauthorizedGain;
        this.unauthorizedGainPercentage = unauthorizedGainPercentage;
    }

    public String getTradingCode() {
        return tradingCode;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public double getUnauthorizedGain() {
        return unauthorizedGain;
    }

    public double getUnauthorizedGainPercentage() {
        return unauthorizedGainPercentage;
    }
    public void printStockData() {
        System.out.println("Trading Code: " + tradingCode);
        System.out.println("Purchase Price: " + purchasePrice);
        System.out.println("Current Price: " + currentPrice);
        System.out.println("Quantity: " + quantity);
        System.out.println("Total Value: " + totalValue);
        System.out.println("Unauthorized Gain: " + unauthorizedGain);
        System.out.println("Unauthorized Gain Percentage: " + unauthorizedGainPercentage);
    }
}
