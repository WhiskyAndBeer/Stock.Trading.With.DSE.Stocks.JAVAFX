module com.example.stocks {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.jsoup;
    requires javafx.web;


    opens com.example.stocks to javafx.fxml;
    exports com.example.stocks;
}