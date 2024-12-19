package com.example.stocks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;

public class DSEStockScraper1 {

    public static void main(String[] args) {
//        String url = "https://www.dsebd.org/displayCompany.php?name=GP";
        String url = "https://www.dsebd.org/displayCompany.php?name=";
        String[] tradingcodes = {"ACI","GP", "BATBC", "BRACBANK", "BRACSCBOND", "BXPHARMA", "BEXIMCO", "BSC", "BATASHOE", "BSCCL", "BANKASIA", "BEXTEX", "BDCOM", "BDFINANCE", "BDLAMPS", "BDTHAI", "BDWELDING", "BEACHHATCH", "BEACONPHAR", "BENGALWTL", "BERGERPBL", "BEXIMLT", "BGIC", "BIFC", "BNICL", "BPML", "BRACBANKL", "BRACSCBOND", "BSCCL", "BXPHARMA", "BXSYNTH", "BXTEXTILE", "CENTRALINS", "CENTRALPHL", "CITYBANK", "CITYGENINS", "CMCKAMAL", "CONFIDCEM", "CONTININS", "CVOPRL", "DACCADYE", "DAFODILCOM", "DBH", "DBH1STMF", "DELTALIFE", "DELTASPINN", "DESCO", "DESHBANDHU", "DHAKABANK", "DHAKAINS", "DOREENPWR", "DSHGARME", "DSSL", "DULAMIACOT", "DUTCHBANGL", "EASTERNINS", "EASTLAND", "EASTRNLUB", "EBL", "EBL1STMF", "EBLNRBMF", "ECABLES", "EHL", "EMERALDOIL", "ENVOYTEX", "ESQUIRENIT", "ETL", "EXIM1STMF", "EXIMBANK", "FAMILYTEX", "FARCHEM", "FAREASTFIN", "FAREASTLIF", "FASFIN", "FBFIF", "FEDERALINS", "FEKDIL", "FINEFOODS", "FIRSTSBANK", "FORTUNE", "FUWANGCER", "FUWANGFOOD", "GBBPOWER", "GEMINISEA", "GENNEXT", "GHAILL", "GHCL", "GLAXOSMITH", "GLOBALINS", "GOLDENSON"};
        //"GP", "BATBC", "BRACBANK", "BRACSCBOND", "BXPHARMA", "BEXIMCO", "BSC", "BATASHOE", "BSCCL", "BANKASIA", "BEXTEX", "BDCOM", "BDFINANCE", "BDLAMPS", "BDTHAI", "BDWELDING", "BEACHHATCH", "BEACONPHAR", "BENGALWTL", "BERGERPBL", "BEXIMLT", "BGIC", "BIFC", "BNICL", "BPML", "BRACBANKL", "BRACSCBOND", "BSCCL", "BXPHARMA", "BXSYNTH", "BXTEXTILE", "CENTRALINS", "CENTRALPHL", "CITYBANK", "CITYGENINS", "CMCKAMAL", "CONFIDCEM", "CONTININS", "CVOPRL", "DACCADYE", "DAFODILCOM", "DBH", "DBH1STMF", "DELTALIFE", "DELTASPINN", "DESCO", "DESHBANDHU", "DHAKABANK", "DHAKAINS", "DOREENPWR", "DSHGARME", "DSSL", "DULAMIACOT", "DUTCHBANGL", "EASTERNINS", "EASTLAND", "EASTRNLUB", "EBL", "EBL1STMF", "EBLNRBMF", "ECABLES", "EHL", "EMERALDOIL", "ENVOYTEX", "ESQUIRENIT", "ETL", "EXIM1STMF", "EXIMBANK", "FAMILYTEX", "FARCHEM", "FAREASTFIN", "FAREASTLIF", "FASFIN", "FBFIF", "FEDERALINS", "FEKDIL", "FINEFOODS", "FIRSTSBANK", "FORTUNE", "FUWANGCER", "FUWANGFOOD", "GBBPOWER", "GEMINISEA", "GENNEXT", "GHAILL", "GHCL", "GLAXOSMITH", "GLOBALINS", "GOLDENSON"
        Arrays.sort(tradingcodes);
        try {
            for (String tradingcode : tradingcodes) {
                // Connect to the website and get the HTML document
                Document doc = Jsoup.connect(url+tradingcode).get();

                ////////////////////////////////////////////////
                String companyName = new String();

                Element companyNameElement = doc.selectFirst("h2.BodyHead.topBodyHead i");

                if (companyNameElement != null) {
                    // Extract and print the company name
                    companyName = companyNameElement.text();
                    System.out.println(companyName);
                } else {
                    System.out.println("Could not find the company name on the page.");
                }
                ////////////////////////////////////////////////
                // Select all rows in the table
                Elements rows = doc.select("tr");

                // Variables to store extracted data
                String lastTradingPrice = null;//
                String closingPrice = null;//
                String lastUpdate = null;
                String daysRange = null;
                String changeValue = null;
                String changePercent = null;
                String daysValue = null;//
                String weeksMovingRange = null;//
                String openingPrice = null;//
                String adjustedOpeningPrice = null;
                String yesterdaysClosingPrice = null;
                String daysVolume = null;
                String daysTrade = null;
                String marketCapitalization = null;//

                // Iterate through the rows to extract the data
                for (Element row : rows) {
                    Elements header = row.select("th");
                    Elements data = row.select("td");

                    if (header.size() > 0 && data.size() > 0) {
                        String headerText = header.get(0).text();
                        switch (headerText) {
                            case "Last Trading Price":
                                lastTradingPrice = data.get(0).text();
                                closingPrice = data.get(1).text();
                                break;
                            case "Last Update":
                                lastUpdate = data.get(0).text();
                                daysRange = data.get(1).text();
                                break;
                            case "Change*":
                                changeValue = data.get(0).text();
                                changePercent = rows.get(rows.indexOf(row) + 1).select("td").get(0).text();
                                break;
                            case "Day's Value (mn)":
                                if (data.size() > 0) { // Added check to ensure data exists
                                    daysValue = data.get(0).text();
                                }
                                break;
                            case "52 Weeks' Moving Range":
                                weeksMovingRange = data.get(1).text();
                                break;
                            case "Opening Price":
                                openingPrice = data.get(0).text();
                                daysVolume = data.get(1).text();
                                break;
                            case "Adjusted Opening Price":
                                adjustedOpeningPrice = data.get(0).text();
                                daysTrade = data.get(1).text();
                                break;
                            case "Yesterday's Closing Price":
                                yesterdaysClosingPrice = data.get(0).text();
                                marketCapitalization = data.get(1).text();
                                break;
                        }
                    }
                }
                System.out.println("Last Trading Price: " + lastTradingPrice);
                System.out.println("Closing Price: " + closingPrice);
                System.out.println("Last Update: " + lastUpdate);
                System.out.println("Days Range: " + daysRange);
                System.out.println("Change Value: " + changeValue);
                System.out.println("Change Percent: " + changePercent);
                System.out.println("Days Value: " + daysValue);
                System.out.println("Weeks Moving Range: " + weeksMovingRange);
                System.out.println("Opening Price: " + openingPrice);
                System.out.println("Adjusted Opening Price: " + adjustedOpeningPrice);
                System.out.println("Yesterday's Closing Price: " + yesterdaysClosingPrice);
                System.out.println("Days Volume: " + daysVolume);
                System.out.println("Days Trade: " + daysTrade);
                System.out.println("Market Capitalization: " + marketCapitalization);
            }



        } catch (Exception e) {
            System.err.println("Error fetching stock data: " + e.getMessage());
        }
    }
}
