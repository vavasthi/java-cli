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
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoadCorporateEvents {
    private String mongoUrl = "mongodb://localhost";
    private final String database = "capitalMarkets";
    private final MongoClient mongoClient = getMongoClient();
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        File file = new File("/Users/vavasthi/Downloads/CF-CA-equities-01-01-1980-to-10-11-2024.csv");
        LoadCorporateEvents lce = new LoadCorporateEvents();
        lce.parseCorporateEvents(file);
    }
    private void parseCorporateEvents(File csv) throws IOException {

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
                .setHeader("symbol","name", "series", "purpose", "faceValue", "exDate", "recordDate", "bookClosureStartDate", "bookClosureEndDate").build();
        CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), format);
        MongoCollection<StockPrice> collection = mongoClient.getDatabase(database).getCollection("stockPrice", StockPrice.class);
        for (CSVRecord csvRecord:parser) {
            try {
                String symbol = csvRecord.get("symbol");
                String series = csvRecord.get("series");
                String purpose = csvRecord.get("purpose");
                float faceValue = -1;
                try {

                    faceValue = Float.parseFloat(csvRecord.get("faceValue"));
                }
                catch (NumberFormatException e) {

                }
                Date exDate = Date.from(LocalDate.parse(csvRecord.get("exDate"), ddmmyyFormat).atStartOfDay().toInstant(currentZoneOffset));
                String recordDate = csvRecord.get("recordDate");
                Date bookClosureStartDate = exDate;
                try {

                    bookClosureStartDate = Date.from(LocalDate.parse(csvRecord.get("bookClosureStartDate"), ddmmyyFormat).atStartOfDay().toInstant(currentZoneOffset));
                }
                catch (DateTimeParseException e) {

                }
                Date bookClosureEndDate = bookClosureStartDate;
                try {

                    bookClosureEndDate = Date.from(LocalDate.parse(csvRecord.get("bookClosureEndDate"), ddmmyyFormat).atStartOfDay().toInstant(currentZoneOffset));
                }
                catch (DateTimeParseException e) {

                }
                decodePurpose(purpose);

//                StockPrice sp = new StockPrice(UUID.randomUUID(), symbol, series, open, high, low, close, last, prevClose, totalTransactedQuantity, totalTransactedValue, timestamp, noTrades, isin);
//                collection.insertOne(sp);
            }
            catch (Exception e) {
                System.out.println(csvRecord.toString());
                System.out.println(String.format("Error is file %s -> %s", csv.getName(), e.toString()));
                throw e;
            }
        }
    }
    private void decodePurpose(String purpose) {

        float dividend = 0;
        float dividendRs = 0;
        float bonus = 0;
        boolean agm = false;
        boolean bc = false;
        float rights = 0;
        float split = 0;
        purpose = purpose.replace("Annual General Meeting","agm");
        {

            Pattern dividendPattern = Pattern.compile("split.*Rs[ \\,]+(\\d+).*To.*Rs[ \\,]+(\\d+).*", Pattern.CASE_INSENSITIVE);
            Matcher matcher = dividendPattern.matcher(purpose);
            if (matcher.find()) {

                String temp1 = matcher.group(1);
                String temp2 = matcher.group(2);
            }
        }
        {

            Pattern dividendPattern = Pattern.compile("agm", Pattern.CASE_INSENSITIVE);
            Matcher matcher = dividendPattern.matcher(purpose);
            if (matcher.find()) {
                agm = true;
            }
        }
        {

            Pattern dividendPattern = Pattern.compile("Annual\\s+Book\\s+Closure", Pattern.CASE_INSENSITIVE);
            Matcher matcher = dividendPattern.matcher(purpose);
            if (matcher.find()) {
                bc = true;
            }
        }
        {
            Pattern dividendPattern = Pattern.compile("Div.*[ \\-]+(\\d+) ?%");
            Matcher matcher = dividendPattern.matcher(purpose);
            if (matcher.find()) {
                dividend = Float.parseFloat(matcher.group(1)) / 100;
            }
        }
        {
            Pattern dividendPattern = Pattern.compile("Div.*R[es][ \\.]+(\\d+) Per +Share", Pattern.CASE_INSENSITIVE);
            Matcher matcher = dividendPattern.matcher(purpose);
            if (matcher.find()) {
                dividendRs = Float.parseFloat(matcher.group(1));
                System.out.println(purpose + " --> " + dividendRs);
            }
        }
        {

            Pattern bonusPattern = Pattern.compile("Bonus[ \\-]+ (\\d+)[ :]+(\\d+)");
            Matcher matcher = bonusPattern.matcher(purpose);
            if (matcher.find()) {
                String temp1 = matcher.group(1);
                String temp2 = matcher.group(2);
                bonus = Float.parseFloat(temp2) / Float.parseFloat(temp1);
            }
        }
        {

            Pattern bonusPattern = Pattern.compile("Right.*[ \\-]+ (\\d+)[ :]+(\\d+)");
            Matcher matcher = bonusPattern.matcher(purpose);
            if (matcher.find()) {
                String temp1 = matcher.group(1);
                String temp2 = matcher.group(2);
                bonus = Float.parseFloat(temp2) / Float.parseFloat(temp1);
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