package org.avasthi.java.cli;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.avasthi.java.cli.pojos.StockMaster;
import org.avasthi.java.cli.pojos.StockPrice;
import org.bson.Document;
import org.bson.UuidRepresentation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;


public class LoadBhavcopy {
    private String mongoUrl = "mongodb://localhost";
    private final String oldBhavcopyPattern = "cm.*bhav\\.csv";
    private final String newBhavcopyPattern = "BhavCopy_NSE_CM.*\\.csv";
    private final String database = "capitalMarkets";
    private final MongoClient mongoClient = getMongoClient();
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        LoadBhavcopy lsm = new LoadBhavcopy();
        File directory = new File("/Users/vavasthi/Downloads/Bhavcopy");
        LoadBhavcopy lbc = new LoadBhavcopy();
        lbc.parseBhavcopy(directory);
    }
    private void parseBhavcopy(File directory) throws IOException {

        for (File file : directory.listFiles()) {
            if (Pattern.matches(oldBhavcopyPattern, file.getName())) {
                parseAndLoadOldBhavcopy(file);
            } else if (Pattern.matches(newBhavcopyPattern, file.getName())) {
                parseAndLoadNewBhavcopy(file);
            }
        }
    }
    private void parseAndLoadOldBhavcopy(File csv) throws IOException {
        DateTimeFormatter ddmmyyFormat = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .parseLenient()
                .appendPattern("dd-MMM-yyyy")
                .toFormatter(Locale.ENGLISH);
        ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
        CSVFormat format = CSVFormat
                .DEFAULT
                .builder()
                .setSkipHeaderRecord(true)
                .setHeader("symbol", "series", "open", "high", "low", "close", "last", "prevClose", "totalTransactedQuantity","totalTransactedValue","timestamp", "noTrades", "isin").build();
        CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), format);
        MongoCollection<StockPrice> collection = mongoClient.getDatabase(database).getCollection("stockPrice", StockPrice.class);
        for (CSVRecord csvRecord:parser) {
            try {
                String symbol = csvRecord.get("symbol");
                String series = csvRecord.get("series");
                float open = Float.parseFloat(csvRecord.get("open"));
                float high = Float.parseFloat(csvRecord.get("high"));
                float low = Float.parseFloat(csvRecord.get("low"));
                float close = Float.parseFloat(csvRecord.get("close"));
                float last = Float.parseFloat(csvRecord.get("last"));
                float prevClose = Float.parseFloat(csvRecord.get("prevClose"));
                long totalTransactedQuantity = Long.parseLong(csvRecord.get("totalTransactedQuantity"));
                float totalTransactedValue = Float.parseFloat(csvRecord.get("totalTransactedValue"));
                Date timestamp = Date.from(LocalDate.parse(csvRecord.get("timestamp"), ddmmyyFormat).atStartOfDay().toInstant(currentZoneOffset));
                int noTrades = -1;
                try {

                    noTrades  = Integer.parseInt(csvRecord.get("noTrades"));
                }
                catch (NumberFormatException nfe) {

                }
                String isin = "";
                try {

                    isin = csvRecord.get("isin");
                }
                catch (IllegalArgumentException iae) {
                    Document filter = new Document("symbol", symbol);
                    StockMaster sm = mongoClient.getDatabase(database).getCollection("stockMaster", StockMaster.class).find(filter).first();
                    if (sm != null) {
                        System.out.println("ISIN is missing replacing with " + sm.isin());
                        isin = sm.isin();
                    }
                }
                StockPrice sp = new StockPrice(UUID.randomUUID(), symbol, series, open, high, low, close, last, prevClose, totalTransactedQuantity, totalTransactedValue, timestamp, noTrades, isin);
                collection.insertOne(sp);
            }
            catch (Exception e) {
                System.out.println(String.format("Error is file %s -> %s", csv.getName(), e.toString()));
                throw e;
            }
        }
    }
    private void parseAndLoadNewBhavcopy(File csv) throws IOException {

        DateTimeFormatter ddmmyyFormat = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .parseLenient()
                .appendPattern("yyyy-MM-dd")
                .toFormatter(Locale.ENGLISH);
        ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
        CSVFormat format = CSVFormat
                .DEFAULT
                .builder()
                .setSkipHeaderRecord(true)
                .setHeader("timestamp", "bizdt", "segment", "src", "type", "instrumnentid", "isin", "symbol", "series", "expiryDate", "financialInstrumentExpiryDate", "stockPrice", "optionType", "name", "open", "high", "low", "close", "last", "prevClose", "underlyingPrice", "settlementPrice", "optionInterest", "changeInOptionInterest",  "totalTransactedQuantity","totalTransactedValue", "noTrades").build();
        CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), format);
        MongoCollection<StockPrice> collection = mongoClient.getDatabase(database).getCollection("stockPrice", StockPrice.class);
        for (CSVRecord csvRecord:parser) {
            try {
                String symbol = csvRecord.get("symbol");
                String series = csvRecord.get("series");
                float open = Float.parseFloat(csvRecord.get("open"));
                float high = Float.parseFloat(csvRecord.get("high"));
                float low = Float.parseFloat(csvRecord.get("low"));
                float close = Float.parseFloat(csvRecord.get("close"));
                float last = close;
                try {

                    last = Float.parseFloat(csvRecord.get("last"));
                }
                catch (Exception e) {

                }
                float prevClose = Float.parseFloat(csvRecord.get("prevClose"));
                long totalTransactedQuantity = Long.parseLong(csvRecord.get("totalTransactedQuantity"));
                float totalTransactedValue = Float.parseFloat(csvRecord.get("totalTransactedValue"));
                Date timestamp = Date.from(LocalDate.parse(csvRecord.get("timestamp"), ddmmyyFormat).atStartOfDay().toInstant(currentZoneOffset));
                int noTrades = -1;
                try {

                    noTrades  = Integer.parseInt(csvRecord.get("noTrades"));
                }
                catch (NumberFormatException nfe) {

                }
                String isin = "";
                try {

                    isin = csvRecord.get("isin");
                }
                catch (IllegalArgumentException iae) {
                    Document filter = new Document("symbol", symbol);
                    StockMaster sm = mongoClient.getDatabase(database).getCollection("stockMaster", StockMaster.class).find(filter).first();
                    if (sm != null) {
                        System.out.println("ISIN is missing replacing with " + sm.isin());
                        isin = sm.isin();
                    }
                }
                StockPrice sp = new StockPrice(UUID.randomUUID(), symbol, series, open, high, low, close, last, prevClose, totalTransactedQuantity, totalTransactedValue, timestamp, noTrades, isin);
                collection.insertOne(sp);
            }
            catch (Exception e) {
                System.out.println(csvRecord.toString());
                System.out.println(String.format("Error is file %s -> %s", csv.getName(), e.toString()));
                throw e;
            }
        }
    }
    private MongoClient getMongoClient() {
        return MongoClients.create(
                MongoClientSettings.builder().applyConnectionString(new ConnectionString(mongoUrl))
                        .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                        .build()
        );
    }
}