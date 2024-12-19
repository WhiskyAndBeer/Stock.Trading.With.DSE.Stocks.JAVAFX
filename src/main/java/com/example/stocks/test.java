package com.example.stocks;


import java.sql.*;




public class test  {

    public static void main(String[] args) throws SQLException {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/login",
                    "root", "12345678");
            System.out.println("Connected to MySQL database");
            Statement statement = connection.createStatement();
            System.out.println("Statement created");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            System.out.println("Query executed");
            if (!resultSet.isBeforeFirst()) {
                System.out.println("No data found in result set.");
            }
            while (resultSet.next()) {
                System.out.println("Next result set ok");
                System.out.println(resultSet.getString("username"));
                System.out.println(resultSet.getString("password"));
                System.out.println("Result set printed 1");
            }
        }
        catch (SQLException e){ // error in this line
            e.printStackTrace();

        }


    }

}
