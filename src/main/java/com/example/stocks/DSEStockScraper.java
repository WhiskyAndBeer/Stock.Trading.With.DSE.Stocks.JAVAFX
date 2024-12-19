package com.example.stocks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

public class DSEStockScraper {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/stockmanagement";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345678";

    private static final String BASE_URL = "https://www.dsebd.org/displayCompany.php?name=";
    private static final String[] COMPANIES = {
            "1JANATAMF", "1STPRIMFMF", "AAMRANET", "AAMRATECH", "ABB1STMF", "ABBANK", "ACFL", "ACI",
            "ACIFORMULA", "ACMELAB", "ACMEPL", "ACTIVEFINE", "ADNTEL", "ADVENT", "AFCAGRO", "AFTABAUTO",
            "AGNISYSL", "AGRANINS", "AIBL1STIMF", "AIL", "AL-HAJTEX", "ALARABANK", "ALIF", "ALLTEX",
            "AMANFEED", "AMBEEPHA", "AMCL(PRAN)", "ANLIMAYARN", "ANWARGALV", "AOL", "APEXFOODS",
            "APEXFOOT", "APEXSPINN", "APEXTANRY", "APOLOISPAT", "ARAMIT", "ARAMITCEM", "ARGONDENIM",
            "ASIAINS", "ASIAPACINS", "ASIATICLAB", "ATCSLGF", "ATLASBANG", "AZIZPIPES", "BANGAS",
            "BANKASIA", "BARKAPOWER", "BATASHOE", "BATBC", "BAYLEASING", "BBS", "BBSCABLES", "BDAUTOCA",
            "BDCOM", "BDFINANCE", "BDLAMPS", "BDSERVICE", "BDTHAI", "BDTHAIFOOD", "BDWELDING", "BEACHHATCH",
            "BEACONPHAR", "BENGALWTL", "BERGERPBL", "BESTHLDNG", "BEXIMCO", "BGIC", "BIFC", "BNICL", "BPML",
            "BPPL", "BRACBANK", "BSC", "BSCPLC", "BSRMLTD", "BSRMSTEEL", "BXPHARMA", "CAPITECGBF", "CAPMBDBLMF",
            "CAPMIBBLMF", "CENTRALINS", "CENTRALPHL", "CITYBANK", "CITYGENINS", "CLICL", "CNATEX", "CONFIDCEM",
            "CONTININS", "COPPERTECH", "CROWNCEMNT", "CRYSTALINS", "CVOPRL", "DACCADYE", "DAFODILCOM", "DBH",
            "DBH1STMF", "DELTALIFE", "DELTASPINN", "DESCO", "DESHBANDHU", "DGIC", "DHAKABANK", "DHAKAINS",
            "DOMINAGE", "DOREENPWR", "DSHGARME", "DSSL", "DULAMIACOT", "DUTCHBANGL", "EASTERNINS", "EASTLAND",
            "EASTRNLUB", "EBL", "EBL1STMF", "EBLNRBMF", "ECABLES", "EGEN", "EHL", "EIL", "EMERALDOIL", "ENVOYTEX",
            "EPGL", "ESQUIRENIT", "ETL", "EXIM1STMF", "EXIMBANK", "FAMILYTEX", "FARCHEM", "FAREASTFIN",
            "FAREASTLIF", "FASFIN", "FBFIF", "FEDERALINS", "FEKDIL", "FINEFOODS", "FIRSTFIN", "FIRSTSBANK",
            "FORTUNE", "FUWANGCER", "FUWANGFOOD", "GBBPOWER", "GEMINISEA", "GENEXIL", "GENNEXT", "GHAIL",
            "GHCL", "GIB", "GLDNJMF", "GLOBALINS", "GOLDENSON", "GP", "GPHISPAT", "GQBALLPEN", "GRAMEENS2",
            "GREENDELMF", "GREENDELT", "GSPFINANCE", "HAKKANIPUL", "HAMI", "HEIDELBCEM", "HFL", "HRTEX",
            "HWAWELLTEX", "IBNSINA", "IBP", "ICB", "ICB3RDNRB", "ICBAGRANI1", "ICBAMCL2ND", "ICBEPMF1S1",
            "ICBIBANK", "ICBSONALI1", "ICICL", "IDLC", "IFADAUTOS", "IFIC", "IFIC1STMF", "IFILISLMF1",
            "ILFSL", "INDEXAGRO", "INTECH", "INTRACO", "IPDC", "ISLAMIBANK", "ISLAMICFIN", "ISLAMIINS",
            "ISNLTD", "ITC", "JAMUNABANK", "JAMUNAOIL", "JANATAINS", "JHRML", "JMISMDL", "JUTESPINN",
            "KARNAPHULI", "KAY&QUE", "KBPPWBIL", "KDSALTD", "KEYACOSMET", "KOHINOOR", "KPCL", "KPPL",
            "KTL", "LANKABAFIN", "LEGACYFOOT", "LHB", "LIBRAINFU", "LINDEBD", "LOVELLO", "LRBDL", "LRGLOBMF1",
            "MAKSONSPIN", "MALEKSPIN", "MARICO", "MATINSPINN", "MBL1STMF", "MEGCONMILK", "MEGHNACEM", "MEGHNAINS",
            "MEGHNALIFE", "MEGHNAPET", "MERCANBANK", "MERCINS", "METROSPIN", "MHSML", "MIDASFIN", "MIDLANDBNK",
            "MIRACLEIND", "MIRAKHTER", "MITHUNKNIT", "MJLBD", "MLDYEING", "MONNOAGML", "MONNOCERA", "MONNOFABR",
            "MONOSPOOL", "MPETROLEUM", "MTB", "NAHEEACP", "NATLIFEINS", "NAVANACNG", "NAVANAPHAR", "NBL",
            "NCCBANK", "NCCBLMF1", "NEWLINE", "NFML", "NHFIL", "NITOLINS", "NORTHERN", "NORTHRNINS", "NPOLYMER",
            "NRBBANK", "NRBCBANK", "NTC", "NTLTUBES", "NURANI", "OAL", "OIMEX", "OLYMPIC", "ONEBANKPLC",
            "ORIONINFU", "ORIONPHARM", "PADMALIFE", "PADMAOIL", "PAPERPROC", "PARAMOUNT", "PDL", "PENINSULA",
            "PEOPLESINS", "PF1STMF", "PHARMAID", "PHENIXINS", "PHOENIXFIN", "PHPMF1", "PIONEERINS", "PLFSL",
            "POPULAR1MF", "POPULARLIF", "POWERGRID", "PRAGATIINS", "PRAGATILIF", "PREMIERBAN", "PREMIERCEM",
            "PREMIERLEA", "PRIME1ICBA", "PRIMEBANK", "PRIMEFIN", "PRIMEINSUR", "PRIMELIFE", "PRIMETEX",
            "PROGRESLIF", "PROVATIINS", "PTL", "PUBALIBANK", "PURABIGEN", "QUASEMIND", "QUEENSOUTH", "RAHIMAFOOD",
            "RAHIMTEXT", "RAKCERAMIC", "RANFOUNDRY", "RDFOOD", "RECKITTBEN", "REGENTTEX", "RELIANCE1",
            "RELIANCINS", "RENATA", "RENWICKJA", "REPUBLIC", "RINGSHINE", "ROBI", "RSRMSTEEL", "RUNNERAUTO",
            "RUPALIBANK", "RUPALIINS", "RUPALILIFE", "SAFKOSPINN", "SAIFPOWER", "SAIHAMCOT", "SAIHAMTEX",
            "SALAMCRST", "SALVOCHEM", "SAMATALETH", "SAMORITA", "SANDHANINS", "SAPORTL", "SAVAREFR", "SBACBANK",
            "SEAPEARL", "SEMLFBSLGF", "SEMLIBBLSF", "SEMLLECMF", "SHAHJABANK", "SHARPIND", "SHASHADNIM",
            "SHEPHERD", "SHURWID", "SHYAMPSUG", "SIBL", "SICL", "SILCOPHL", "SILVAPHL", "SIMTEX", "SINGERBD",
            "SINOBANGLA", "SIPLC", "SKTRIMS", "SONALIANSH", "SONALILIFE", "SONALIPAPR", "SONARBAINS",
            "SONARGAON", "SOUTHEASTB", "SPCERAMICS", "SPCL", "SQUARETEXT", "SQURPHARMA", "SSSTEEL", "STANCERAM",
            "STANDARINS", "STANDBANKL", "STYLECRAFT", "SUMITPOWER", "SUNLIFEINS", "TAKAFULINS", "TALLUSPIN",
            "TAMIJTEX", "TECHNODRUG", "TILIL", "TITASGAS", "TOSRIFA", "TRUSTB1MF", "TRUSTBANK", "TUNGHAI",
            "UCB", "UNILEVERCL", "UNIONBANK", "UNIONCAP", "UNIONINS", "UNIQUEHRL", "UNITEDFIN", "UNITEDINS",
            "UPGDCL", "USMANIAGL", "UTTARABANK", "UTTARAFIN", "VAMLBDMF1", "VAMLRBBF", "VFSTDL", "WALTONHIL",
            "WATACHEM", "WMSHIPYARD", "YPL", "ZAHEENSPIN", "ZAHINTEX", "ZEALBANGLA"
    };
//private static final String[] COMPANIES = {
//        "ACI", "BATBC", "BEXIMCO"
//};

    public static void syncDatabaseWithCompanies() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            List<String> companyList = Arrays.asList(COMPANIES);

            // Delete old entries
            deleteOldEntries(connection, companyList);

            // Add new entries
            addNewEntries(connection, companyList);

            // Fetch and update stock data
            fetchAndStoreStockData(connection);
        } catch (Exception e) {
            System.err.println("Error syncing database: " + e.getMessage());
        }
    }

    private static void deleteOldEntries(Connection connection, List<String> companyList) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM stock_data WHERE trading_code NOT IN (" +
                        String.join(",", companyList.stream().map(c -> "?").toArray(String[]::new)) + ")")) {
            for (int i = 0; i < companyList.size(); i++) {
                statement.setString(i + 1, companyList.get(i));
            }
            int rowsDeleted = statement.executeUpdate();
            System.out.println(rowsDeleted + " old entries deleted.");
        } catch (Exception e) {
            System.err.println("Error deleting old entries: " + e.getMessage());
        }
    }

    private static void addNewEntries(Connection connection, List<String> companyList) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT IGNORE INTO stock_data (trading_code) VALUES (?)")) {
            for (String company : companyList) {
                statement.setString(1, company);
                statement.executeUpdate();
            }
            System.out.println("New entries added to the database.");
        } catch (Exception e) {
            System.err.println("Error adding new entries: " + e.getMessage());
        }
    }

    private static void fetchAndStoreStockData(Connection connection) {
        for (String company : COMPANIES) {
            System.out.println("Fetching data for " + company);
            try {
                Document doc = Jsoup.connect(BASE_URL + company).get();

                String lastPrice = null;
                String changeValue = null;
                String changePercent = null;

                Elements rows = doc.select("tr");
                for (Element row : rows) {
                    Elements header = row.select("th");
                    Elements data = row.select("td");

                    if (header.size() > 0 && data.size() > 0) {
                        String headerText = header.get(0).text();
                        switch (headerText) {
                            case "Last Trading Price":
                                lastPrice = data.get(0).text();
                                break;
                            case "Change*":
                                changeValue = data.get(0).text();
                                changePercent = rows.get(rows.indexOf(row) + 1).select("td").get(0).text();
                                break;
                        }
                    }
                }

                // Update stock data
                updateStockData(connection, company, lastPrice, changeValue, changePercent);
            } catch (Exception e) {
                System.err.println("Error fetching data for " + company + ": " + e.getMessage());
            }
        }
    }

    private static void updateStockData(Connection connection, String tradingCode, String lastPrice, String changeValue, String changePercent) {
        String query = "UPDATE stock_data SET last_price = ?, change_value = ?, change_percent = ?, last_updated = CURRENT_TIMESTAMP WHERE trading_code = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, lastPrice);
            preparedStatement.setString(2, changeValue);
            preparedStatement.setString(3, changePercent);
            preparedStatement.setString(4, tradingCode);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error updating stock data for " + tradingCode + ": " + e.getMessage());
        }
    }
    double CurrentPrice(String tradingcode) {
        System.out.println("Fetching data for " + tradingcode);
        String url = "https://www.dsebd.org/displayCompany.php?name=";
        try {
            // Connect to the website and get the HTML document
            Document doc = Jsoup.connect(url + tradingcode).get();

            Elements rows = doc.select("tr");

            // Variables to store extracted data
            String lastTradingPrice = null;//


            // Iterate through the rows to extract the data
            for (Element row : rows) {
                Elements header = row.select("th");
                Elements data = row.select("td");

                if (header.size() > 0 && data.size() > 0) {
                    String headerText = header.get(0).text();
                    switch (headerText) {
                        case "Last Trading Price":
                            lastTradingPrice = data.get(0).text();
                            double price = Double.parseDouble(lastTradingPrice);
                            System.out.println("Price is like : " + price);
                            return price;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching stock data: " + e.getMessage());
        }
        return 0;
    }
}