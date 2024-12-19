package com.example.stocks;

public class Stock {
    private String tradingCode;
    private String lastPrice;
    private String changeValue;
    private String changePercent;

    public Stock(String tradingCode, String lastPrice, String changeValue, String changePercent) {
        this.tradingCode = tradingCode;
        this.lastPrice = lastPrice;
        this.changeValue = changeValue;
        this.changePercent = changePercent;
    }

    public String getTradingCode() {
        return tradingCode;
    }

    public void setTradingCode(String tradingCode) {
        this.tradingCode = tradingCode;
    }

    public String getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;
    }

    public String getChangeValue() {
        return changeValue;
    }

    public void setChangeValue(String changeValue) {
        this.changeValue = changeValue;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }
}
