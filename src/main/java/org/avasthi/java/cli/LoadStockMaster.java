package org.avasthi.java.cli;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.avasthi.java.cli.pojos.StockMaster;
import org.bson.Document;
import org.bson.UuidRepresentation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class LoadStockMaster extends Base{
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        LoadStockMaster lsm = new LoadStockMaster();
        lsm.parseCsvFile("/Users/vavasthi/Downloads/EQUITY_L.CSV");
    }
    private void parseCsvFile(String path) throws IOException {

        DateTimeFormatter ddmmyyFormat = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .parseLenient()
                .appendPattern("dd-MMM-yyyy")
                .toFormatter(Locale.ENGLISH);
        ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
        File csv = new File(path);
        CSVFormat format = CSVFormat.DEFAULT.builder().setSkipHeaderRecord(true).setHeader("symbol", "name", "series", "dateOfListing", "paidUpValue", "marketLot", "isin", "faceValue").build();
        CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), format);
        System.out.println(parser.getHeaderNames());
        MongoCollection<StockMaster> collection = mongoClient.getDatabase(database).getCollection(stockMasterCollection, StockMaster.class);
        for (CSVRecord csvRecord:parser) {
            System.out.println(csvRecord.toString());
            String symbol = csvRecord.get("symbol");
            String name = csvRecord.get("name");
            String series = csvRecord.get("series");
            Date dateOfListing = Date.from(LocalDate.parse(csvRecord.get("dateOfListing"), ddmmyyFormat).atStartOfDay().toInstant(currentZoneOffset));
            float paidUpValue = Float.parseFloat(csvRecord.get("paidUpValue"));
            int marketLot = Integer.parseInt(csvRecord.get("marketLot"));
            String isin = csvRecord.get("isin");
            float faceValue = Float.parseFloat(csvRecord.get("faceValue"));
            StockMaster sm = new StockMaster(UUID.randomUUID(), symbol, name, series, dateOfListing, paidUpValue, marketLot, isin, faceValue);
            System.out.println(sm);
            collection.insertOne(sm);
        }
    }
}