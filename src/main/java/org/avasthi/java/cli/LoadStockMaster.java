package org.avasthi.java.cli;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;


public class LoadStockMaster extends Base{
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        LoadStockMaster lsm = new LoadStockMaster();
        File csv = new File(new File(System.getenv("HOME"), "Downloads"), "equity.csv");
        lsm.parseCsvFile(csv.getPath());
    }
    private void parseCsvFile(String path) throws IOException {

        DateTimeFormatter ddmmyyFormat = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .parseLenient()
                .appendPattern("dd-MMM-yyyy")
                .toFormatter(Locale.ENGLISH);
        ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
        File csv = new File(path);
        CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), CSVFormat.EXCEL);
        System.out.println(parser.getHeaderNames());
        MongoCollection<Document> collection = getMongoClient().getDatabase(database).getCollection(stockMasterCollectionName, Document.class);
        for (CSVRecord csvRecord:parser) {
            String symbol = csvRecord.get(2);
            Document key = new Document("symbol", symbol);
            FindIterable<Document> foundDocuments = collection.find(key);
            for (Document smDocument : foundDocuments) {
                smDocument.append("securityCode", csvRecord.get(0));
                System.out.println(smDocument);
                collection.replaceOne(new Document("_id", smDocument.get("_id")), smDocument);
            }

/*            System.out.println(csvRecord.toString());
            StockMaster sm = new StockMaster(UUID.randomUUID(), , name, series, dateOfListing, paidUpValue, marketLot, isin, faceValue);

            String symbol = csvRecord.get("symbol");
            String name = csvRecord.get("name");
            String series = csvRecord.get("series");
            Date dateOfListing = Date.from(LocalDate.parse(csvRecord.get("dateOfListing"), ddmmyyFormat).atStartOfDay().toInstant(currentZoneOffset));
            float paidUpValue = Float.parseFloat(csvRecord.get("paidUpValue"));
            int marketLot = Integer.parseInt(csvRecord.get("marketLot"));
            String isin = csvRecord.get("isin");
            float faceValue = Float.parseFloat(csvRecord.get("faceValue"));
            System.out.println(sm);
            collection.insertOne(sm);*/
        }
    }
}