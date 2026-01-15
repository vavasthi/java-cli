package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.avasthi.java.cli.pojos.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.regex.Pattern;


public class LoadBhavcopy extends Base {
  private final File baseDirectory = new File("/data/datasets/Bhavcopy");
  private final String stockMasterDirectoryName = "StockMaster";
  private final String bseDirectoryName = "bse";
  private final String niftySymbolsFilename = "MW-NIFTY-50-30-Nov-2025.csv";
  private final String sensexSymbolFilename = "BSESENSEXindex_Constituents.csv";
  private final String bseEqT0Filename = "EQT0.csv";
  private final String bseEqT1Filename = "Equity.csv";
  private final String oldBhavcopyPattern = "cm.*bhav\\.csv";
  private final String newBhavcopyPattern = "BhavCopy_NSE_CM.*\\.csv";
  private final String oldFnOBhavcopyPattern = "fo.*bhav\\.csv";
  private final String newFnOBhavcopyPattern = "BhavCopy_NSE_FO.*\\.csv";
  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    LoadBhavcopy lbc = new LoadBhavcopy();
    lbc.parseBhavcopy();
  }
  private void parseBhavcopy() throws IOException {

    File directory = new File(baseDirectory, "Stocks");
    Map<String, StockMaster> stockMasterMap = loadStockMaster();
    List<StockPrice> stockPriceList = new ArrayList<>();
    for (File file : directory.listFiles()) {
      if (file.isFile() && file.getName().endsWith("csv")) {

        System.out.println(file.getAbsolutePath());
        if (Pattern.matches(oldBhavcopyPattern, file.getName())) {
          parseAndLoadOldBhavcopy(file, stockMasterMap, stockPriceList);
        } else if (Pattern.matches(newBhavcopyPattern, file.getName())) {
          parseAndLoadNewBhavcopy(file, stockMasterMap, stockPriceList);
        }
      }
      if (stockPriceList.size() > 1000) {
        writeRecords(stockPriceList);
      }
    }
    if (stockPriceList.size() > 0) {
      writeRecords(stockPriceList);
    }
  }
  private void writeRecords(List<StockPrice> stockPriceList) {

    try {

      getStockPriceCollection().insertMany(stockPriceList);
      stockPriceList.clear();
    }
    catch (Exception e1) {
        for (StockPrice sp : stockPriceList) {
          try {
            getStockPriceCollection().insertOne(sp);
          }
          catch (Exception e) {

          }
        }
    }
    stockPriceList.clear();
  }
  private void parseFnOBhavcopy() throws IOException {

    File directory = new File(baseDirectory, "FnO");
    Map<String, StockMaster> stockMasterMap = loadStockMaster();
    List<OptionPrice> optionPriceList = new ArrayList<>();
    for (File file : directory.listFiles()) {
      if (file.isFile() && file.getName().endsWith("csv")) {

        System.out.println(file.getAbsolutePath());
        if (Pattern.matches(oldFnOBhavcopyPattern, file.getName())) {
          parseAndLoadOldFnOBhavcopy(file, stockMasterMap, optionPriceList);
        } else if (Pattern.matches(newFnOBhavcopyPattern, file.getName())) {
          parseAndLoadNewFnOBhavcopy(file, stockMasterMap, optionPriceList);
        }
      }
      if (optionPriceList.size() > 1000) {
        getOptionPriceCollection().insertMany(optionPriceList);
        optionPriceList.clear();
      }
    }
    if (optionPriceList.size() > 0) {
      getOptionPriceCollection().insertMany(optionPriceList);
      optionPriceList.clear();
    }
  }
  private Set<String> niftySymbols() throws IOException {

    Set<String> returnValue = new HashSet<>();
    File nifty = new File(new File(baseDirectory, stockMasterDirectoryName),  niftySymbolsFilename);
    int linesToSkip = 2; // Number of header lines to skip
    Scanner scanner = new Scanner(new FileInputStream(nifty));
    scanner.nextLine();
    scanner.nextLine();
    while(scanner.hasNextLine()) {
      String[] s = scanner.nextLine().split(",");
      returnValue.add(StringUtils.strip(s[0], "\""));
    }
    return returnValue;
  }
  private Map<String, String> bseStocks() throws IOException {

    Map<String, String> equityMap = new HashMap<>();
    {
      File eqT0 = new File(new File(new File(baseDirectory, stockMasterDirectoryName), bseDirectoryName), bseEqT0Filename);
      CSVFormat eqT0Format = CSVFormat
              .DEFAULT
              .builder()
              .setSkipHeaderRecord(true)
              .setHeader("stockCode", "name", "securityId","securityName","status", "group", "faceValue", "isin").build();
      CSVParser eqT0Parser = CSVParser.parse(eqT0, Charset.defaultCharset(), eqT0Format);
      for (CSVRecord record : eqT0Parser) {
        String symbol = record.get("stockCode");
        String isin = record.get("isin");
        equityMap.put(isin, symbol);
      }
    }
    {
      File eqT1 = new File(new File(new File(baseDirectory, stockMasterDirectoryName),  bseDirectoryName), bseEqT1Filename);
      CSVFormat eqT1Format = CSVFormat
              .DEFAULT
              .builder()
              .setSkipHeaderRecord(true)
              .setHeader("stockCode", "name", "securityId","securityName","status", "group", "faceValue", "isin").build();
      CSVParser eqT0Parser = CSVParser.parse(eqT1, Charset.defaultCharset(), eqT1Format);
      for (CSVRecord record : eqT0Parser) {
        String symbol = record.get("stockCode");
        String isin = record.get("isin");
        equityMap.put(isin, symbol);
      }
    }
    return equityMap;
  }
  private Map<String, String> sensexSymbols(Map<String, String> equityMap) throws IOException {

    Map<String, String> isinStockCodeMap = new HashMap<>();
    File sensex = new File(new File(new File(baseDirectory, stockMasterDirectoryName), "bse"), sensexSymbolFilename);
    CSVFormat sensexMasterFormat = CSVFormat
            .DEFAULT
            .builder()
            .setSkipHeaderRecord(true)
            .setHeader("stockCode", "name", "isin", "price").build();
    CSVParser parser = CSVParser.parse(sensex, Charset.defaultCharset(), sensexMasterFormat);
    for (CSVRecord record : parser) {
      String symbol = record.get("stockCode");
      String name = record.get("name");
      String isin = record.get("isin");
      isinStockCodeMap.put(isin, symbol);
    }
    isinStockCodeMap.entrySet().forEach(e -> {
      if (!equityMap.containsKey(e.getKey())) {
        System.out.println(e.getKey() + " not found..");
      }
    });
    return isinStockCodeMap;
  }
  private void parseStockMaster() throws IOException {

    List<StockMaster> stockMasters = new ArrayList<>();
    Set<String> niftySymbols = niftySymbols();
    Map<String, String> equityMap = bseStocks();
    Map<String, String> sensexSymbols = sensexSymbols(equityMap);
    File nseMaster = new File(new File(baseDirectory, stockMasterDirectoryName),  "EQUITY_L.csv");
    DateTimeFormatter ddmmyyFormat = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient()
            .appendPattern("dd-MMM-yyyy")
            .toFormatter(Locale.ENGLISH);
    ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
    CSVFormat nseMasterFormat = CSVFormat
            .DEFAULT
            .builder()
            .setSkipHeaderRecord(true)
            .setHeader("symbol", "name", "series", "dateOfListing", "paidUpValue", "marketLot", "isin", "faceValue").build();
    CSVParser parser = CSVParser.parse(nseMaster, Charset.defaultCharset(), nseMasterFormat);
    for (CSVRecord record : parser) {
      String symbol = record.get("symbol");
      String name = record.get("name");
      String series = record.get("series");
      String strTimestamp = record.get("dateOfListing");
      Date dateofListing = Date.from(LocalDate.parse(strTimestamp, ddmmyyFormat).atTime(23, 59, 59).toInstant(currentZoneOffset));
      double paidUpValue = Double.parseDouble(record.get("paidUpValue"));
      int marketLot = Integer.parseInt(record.get("marketLot"));
      String isin = record.get("isin");
      double faceValue = Double.parseDouble(record.get("faceValue"));
      String stockCode = equityMap.get(isin);
      boolean nifty = niftySymbols.contains(symbol);
      boolean sensex = sensexSymbols.containsKey(isin);
      StockMaster sm = new StockMaster(UUID.randomUUID(),
              symbol,
              name,
              series,
              dateofListing,
              paidUpValue,
              marketLot,
              isin,
              faceValue,
              stockCode,
              nifty,
              sensex);
      if (getStockMasterCollection().find(Filters.eq("isin", sm.getIsin())).first() == null) {

        stockMasters.add(sm);
      }
    }
    System.out.println(String.format("Inserting %d records in stock master", stockMasters.size()));
    if (stockMasters.size() > 0) {

      getStockMasterCollection().insertMany(stockMasters);
    }
  }
  private Map<String, StockMaster> loadStockMaster() {
    Map<String, StockMaster> stockMasterMap = new HashMap<>();
    getStockMasterCollection().find().forEach(sm -> {
      stockMasterMap.put(sm.getSymbol(), sm);
    });
    return stockMasterMap;
  }
  private void parseAndLoadOldBhavcopy(File csv,Map<String, StockMaster> stockMasterMap, List<StockPrice> stockPriceList) throws IOException {
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
            .setHeader("symbol", "series", "open", "high", "low", "close", "last", "prevClose", "totalTransactedQuantity","totalTransactedValue","timestamp").build();
    CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), format);
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
        String strTimestamp = csvRecord.get("timestamp");
        Date timestamp = Date.from(LocalDate.parse(strTimestamp, ddmmyyFormat).atTime(23, 59, 59).toInstant(currentZoneOffset));
        StockMaster sm = stockMasterMap.get(symbol);
        if (sm == null) {
          System.out.println(String.format("%s doesn't exist in stock master", symbol));
        }
        else {

          StockPrice sp = new StockPrice(UUID.randomUUID(),
                  symbol,
                  timestamp,
                  series,
                  open,
                  high,
                  low,
                  close,
                  last,
                  prevClose,
                  close,
                  totalTransactedQuantity,
                  totalTransactedValue,
                  sm.getIsin());
          if (sp.series().equals("EQ")) {

            stockPriceList.add(sp);
          }
        }
      }
      catch (Exception e) {
        System.out.println(csvRecord.toString());
        System.out.println(String.format("Error is file %s -> %s", csv.getName(), e.toString()));
        throw e;
      }
    }
  }
  private void parseAndLoadNewBhavcopy(File csv,Map<String,
                                               StockMaster> stockMasterMap,
                                       List<StockPrice> stockPriceList) throws IOException {

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
    MongoCollection<Bhav> collection = mongoClient.getDatabase(database).getCollection(stockPriceCollectionName, Bhav.class);
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
        String strTimestamp = csvRecord.get("timestamp");
        Date timestamp = Date.from(LocalDate.parse(strTimestamp, ddmmyyFormat).atStartOfDay().toInstant(currentZoneOffset));
        long noTrades = -1;
        try {

          noTrades  = Integer.parseInt(csvRecord.get("noTrades"));
        }
        catch (NumberFormatException nfe) {

        }
        StockMaster sm = stockMasterMap.get(symbol);
        if (sm == null) {
          System.out.println(String.format("%s doesn't exist in stock master", symbol));
        }
        else {

          StockPrice sp = new StockPrice(UUID.randomUUID(),
                  symbol,
                  timestamp,
                  series,
                  open,
                  high,
                  low,
                  close,
                  last,
                  prevClose,
                  close,
                  totalTransactedQuantity,
                  totalTransactedValue,
                  sm.getIsin());
          if (sp.series().equals("EQ")) {

            stockPriceList.add(sp);
          }
        }
      }
      catch (Exception e) {
        System.out.println(csvRecord.toString());
        System.out.println(String.format("Error is file %s -> %s", csv.getName(), e.toString()));
        throw e;
      }
    }
  }
  private void parseAndLoadOldFnOBhavcopy(File csv,Map<String, StockMaster> stockMasterMap, List<OptionPrice> optionPriceList) throws IOException {
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
            .setHeader("instrument", "symbol", "expiryDate", "strikePrice", "optionType", "open", "high", "low", "close", "settlePrice", "contracts", "value", "openInterest", "changeInOpenInterest", "date").build();
    CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), format);
    for (CSVRecord csvRecord:parser) {
      try {
        String instrument = csvRecord.get("instrument");
        String symbol = csvRecord.get("symbol");
        String expiryDateStr = csvRecord.get("expiryDate");
        Date expiryDate = Date.from(LocalDate.parse(expiryDateStr, ddmmyyFormat).atTime(23, 59, 59).toInstant(currentZoneOffset));
        float strikePrice = Float.parseFloat(csvRecord.get("strikePrice"));
        String optionType = csvRecord.get("optionType");
        float open = Float.parseFloat(csvRecord.get("open"));
        float high = Float.parseFloat(csvRecord.get("high"));
        float low = Float.parseFloat(csvRecord.get("low"));
        float close = Float.parseFloat(csvRecord.get("close"));
        float settlePrice = Float.parseFloat(csvRecord.get("settlePrice"));
        float contracts = Float.parseFloat(csvRecord.get("contracts"));
        float value = Float.parseFloat(csvRecord.get("value"));
        float openInterest = Float.parseFloat(csvRecord.get("openInterest"));
        float changeInOpenInterest = Float.parseFloat(csvRecord.get("changeInOpenInterest"));
        String strTimestamp = csvRecord.get("date");
        Date date = Date.from(LocalDate.parse(strTimestamp, ddmmyyFormat).atTime(23, 59, 59).toInstant(currentZoneOffset));
        StockMaster sm = stockMasterMap.get(symbol);
        if (sm == null) {
          System.out.println(String.format("%s doesn't exist in stock master", symbol));
        }
        else {

          OptionPrice op = new OptionPrice(UUID.randomUUID(),
                  DerivativeType.getFromOldCode(instrument),
                  OptionType.getFromOldCode(optionType),
                  symbol,
                  expiryDate,
                  strikePrice,
                  open,
                  high,
                  low,
                  close,
                  settlePrice,
                  contracts,
                  value,
                  openInterest,
                  changeInOpenInterest,
                  date,
                  sm.getIsin());
          optionPriceList.add(op);
        }
      }
      catch (Exception e) {
        System.out.println(csvRecord.toString());
        System.out.println(String.format("Error is file %s -> %s", csv.getName(), e.toString()));
        throw e;
      }
    }
  }
  private void parseAndLoadNewFnOBhavcopy(File csv,
                                          Map<String, StockMaster> stockMasterMap,
                                       List<OptionPrice> optionPriceList) throws IOException {

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
            .setHeader("date", "bizdt", "segment", "src", "type", "instrumnentid", "isin", "symbol", "sctySrs", "expiryDate", "financialInstrumentExpiryDate", "strikePrice", "optionType", "name", "open", "high", "low", "close", "last", "prevClose", "underlyingPrice", "settlementPrice", "openInterest", "changeInOpenInterest",  "totalTransactedQuantity","totalTransactedValue", "noTrades").build();
    CSVParser parser = CSVParser.parse(csv, Charset.defaultCharset(), format);
    MongoCollection<Bhav> collection = mongoClient.getDatabase(database).getCollection(stockPriceCollectionName, Bhav.class);
    for (CSVRecord csvRecord:parser) {
      String instrument = csvRecord.get("type");
      String symbol = csvRecord.get("symbol");
      String expiryDateStr = csvRecord.get("expiryDate");
      Date expiryDate = Date.from(LocalDate.parse(expiryDateStr, ddmmyyFormat).atTime(23, 59, 59).toInstant(currentZoneOffset));
      float strikePrice = 0;
      try {

        strikePrice = Float.parseFloat(csvRecord.get("strikePrice"));
      }
      catch (Exception e) {

      }
      String optionType = csvRecord.get("optionType");
      float open = Float.parseFloat(csvRecord.get("open"));
      float high = Float.parseFloat(csvRecord.get("high"));
      float low = Float.parseFloat(csvRecord.get("low"));
      float close = Float.parseFloat(csvRecord.get("close"));
      float settlePrice = Float.parseFloat(csvRecord.get("settlementPrice"));
      float contracts = Float.parseFloat(csvRecord.get("totalTransactedQuantity"));
      float value = Float.parseFloat(csvRecord.get("totalTransactedValue"));
      float openInterest = Float.parseFloat(csvRecord.get("openInterest"));
      float changeInOpenInterest = Float.parseFloat(csvRecord.get("changeInOpenInterest"));
      String strTimestamp = csvRecord.get("date");
      Date date = Date.from(LocalDate.parse(strTimestamp, ddmmyyFormat).atTime(23, 59, 59).toInstant(currentZoneOffset));
      StockMaster sm = stockMasterMap.get(symbol);
      if (sm == null) {
        System.out.println(String.format("%s doesn't exist in stock master", symbol));
      }
      else {

        OptionPrice op = new OptionPrice(UUID.randomUUID(),
                DerivativeType.getFromNewCode(instrument),
                OptionType.getFromNewCode(optionType),
                symbol,
                expiryDate,
                strikePrice,
                open,
                high,
                low,
                close,
                settlePrice,
                contracts,
                value,
                openInterest,
                changeInOpenInterest,
                date,
                sm.getIsin());
        optionPriceList.add(op);
      }
    }
  }
}