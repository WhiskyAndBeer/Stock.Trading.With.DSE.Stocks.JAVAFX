package com.example.stocks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;

public class FetchTradingCode {
    public static void main(String[] args) {
        String url = "https://dsebd.org/latest_share_price_scroll_l.php";
        String outputFileName = "trading_codes.txt";

        try {
            // Fetch the HTML content from the URL
            Document document = Jsoup.connect(url).get();

            // Select all <a> elements within the specific table rows
            Elements tradingCodeElements = document.select("table.shares-table tbody tr td a");

            // Initialize a StringBuilder to store the output
            StringBuilder output = new StringBuilder();

            for (Element element : tradingCodeElements) {
                // Extract the text from each <a> tag and append to the StringBuilder
                String tradingCode = element.text();
                if (output.length() > 0) {
                    output.append(", "); // Add a comma and a space between entries
                }
                output.append("\"").append(tradingCode).append("\"");
            }

            // Write the output to the text file
            try (FileWriter writer = new FileWriter(outputFileName)) {
                writer.write(output.toString());
                System.out.println("Trading codes saved successfully to " + outputFileName);
            }
        } catch (IOException e) {
            System.err.println("Error fetching or writing data: " + e.getMessage());
        }
    }
}
