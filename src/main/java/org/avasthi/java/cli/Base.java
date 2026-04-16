package org.avasthi.java.cli;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertManyResult;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.csv.CSVFormat;
import org.avasthi.java.cli.pojos.*;
import org.avasthi.java.cli.pojos.Currency;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.YEAR;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Base {
  protected static final String mongoUrl = "mongodb://localhost";
  protected final String database = "capitalMarkets";
  protected final String stockMasterCollectionName = "stockMaster";
  protected final String stockPriceCollectionName = "stockPrice";
  protected final String optionPriceCollectionName = "optionPrice";
  protected final String indexPriceCollectionName = "indexPrice";
  protected final String corporateEventCollectionName = "corporateEvents";
  protected final String minuteTickCollectionName = "minuteTick";
  protected final String tradeTickCollectionName = "tradeTick";
  protected final String tradeTickDepthCollectionName = "tradeTickDepth";
  protected final String quarterlyResultsCollectionName = "quarterlyResults";
  protected final String cpiCollectionName = "cpi";
  protected final String iipCollectionName = "iip";
  protected final String currencyCollectionName = "currency";
  protected final String zerodhaInstrumentCollectionName = "zerodhaInstruments";
  protected final String simulatedTradeName = "simulatedTrade";
  protected final String allSimulatedTradeName = "allSimulatedTrade";
  protected final String dailyVix = "dailyVix";
  protected final double riskFreeRate = .10;
  protected static final MongoClient mongoClient = createMongoClient();

  private static MongoClient createMongoClient() {

    return MongoClients.create(
            MongoClientSettings.builder().applyConnectionString(new ConnectionString(mongoUrl))
                    .codecRegistry(fromRegistries(
                            MongoClientSettings.getDefaultCodecRegistry(),
                            fromProviders(PojoCodecProvider.builder().automatic(true).build())
                    ))
                    .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                    .build()
    );
  }
  protected  MongoClient getMongoClient() {
    return mongoClient;
  }
  protected WebDriver getWebDriver() {
    return getWebDriver(true, new HashMap<>());
  }
  protected WebDriver getWebDriver(boolean headless, Map<String, Object> prefs) {
    prefs.put("download.prompt_for_download", false);
    prefs.put("download.directory_upgrade", true);
    prefs.put("safebrowsing_for_trusted_sources_enabled", false);
    prefs.put("safebrowsing.enabled",false);
    prefs.put("excludeSwitches", Arrays.asList("enable-automation"));

    ChromeOptions chromeOptions = new ChromeOptions();
    if (headless) {

      chromeOptions.addArguments("--no-sandbox", "disable-search-engine-choice-screen", "--headless=new");
    }
    else {

      chromeOptions.addArguments("--no-sandbox", "disable-search-engine-choice-screen", "--disable-blink-features=AutomationControlled");
    }
    chromeOptions.setAcceptInsecureCerts(true);
    chromeOptions.setExperimentalOption("prefs", prefs);
    chromeOptions.addArguments("--disable-extensions");
    return new ChromeDriver(chromeOptions);
  }
  protected WebDriver getCFirefoxDriver(boolean headless) {
    Map<String, Object> prefs = new HashMap<>();
    prefs.put("download.prompt_for_download", false);
    prefs.put("download.directory_upgrade", true);
    prefs.put("safebrowsing_for_trusted_sources_enabled", false);
    prefs.put("safebrowsing.enabled",false);
    prefs.put("excludeSwitches", Arrays.asList("enable-automation"));

    FirefoxOptions firefoxOptions = new FirefoxOptions();
    if (headless) {

      firefoxOptions.addArguments("--no-sandbox", "disable-search-engine-choice-screen", "--headless=new");
    }
    else {

      firefoxOptions.addArguments("--no-sandbox", "disable-search-engine-choice-screen", "--disable-blink-features=AutomationControlled");
    }
    firefoxOptions.setAcceptInsecureCerts(true);
    firefoxOptions.addArguments("--disable-extensions");
    return new FirefoxDriver(firefoxOptions);
  }
  protected Headers allReports(OkHttpClient client) {
    String url = "https://www.nseindia.com/companies-listing/corporate-filings-financial-results";
    Request request = new Request.Builder()
            .url(url)
            .headers(defaultHeaders(new Headers.Builder()).build())
            .get()
            .build();
    Headers.Builder nextRequestHeaderBuilder = new Headers.Builder();
    try (Response response = client.newCall(request).execute()) {
      Headers headers = response.headers();
      for (Iterator<Pair<String, String>> it = headers.iterator(); it.hasNext(); ) {
        Pair<String, String> kv = it.next();
        if (kv.getFirst().equalsIgnoreCase("set-cookie")) {
          nextRequestHeaderBuilder.add("Cookie", kv.getSecond());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return nextRequestHeaderBuilder.build();
  }

  protected Headers.Builder defaultHeaders(Headers.Builder builder) {

//    builder.add("Accept-Encoding", "gzip, deflate, br, zstd");
    builder.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
    return builder;
  }

  protected Headers.Builder allHeaders(Headers.Builder builder,
                                       Headers headers) {
    builder = defaultHeaders(builder);
    for (Iterator<Pair<String, String>> it = headers.iterator(); it.hasNext(); ) {
      Pair<String, String> kv = it.next();
      builder.add(kv.getFirst(), kv.getSecond());
    }
    return builder;
  }

  protected MongoCollection<StockPrice> getStockPriceCollection() {
    return getMongoClient().getDatabase(database).getCollection(stockPriceCollectionName, StockPrice.class);
  }
  protected MongoCollection<OptionPrice> getOptionPriceCollection() {
    return getMongoClient().getDatabase(database).getCollection(optionPriceCollectionName, OptionPrice.class);
  }
  protected MongoCollection<IndexPrice> getIndexPriceCollection() {
    return getMongoClient().getDatabase(database).getCollection(indexPriceCollectionName, IndexPrice.class);
  }
  protected MongoCollection<CorporateEvent> getCorporateEventsCollection() {
    return getMongoClient().getDatabase(database).getCollection(corporateEventCollectionName, CorporateEvent.class);
  }
  protected MongoCollection<StockMaster> getStockMasterCollection() {
    return getMongoClient().getDatabase(database).getCollection(stockMasterCollectionName, StockMaster.class);
  }
  protected MongoCollection<MinuteTick> getMinuteTickCollection() {
    return getMongoClient().getDatabase(database).getCollection(minuteTickCollectionName, MinuteTick.class);
  }
  protected MongoCollection<TradeTick> getTradeTickCollection() {
    return getMongoClient().getDatabase(database).getCollection(tradeTickCollectionName, TradeTick.class);
  }
  protected MongoCollection<TradeTickDepth> getTradeTickDepthCollection() {
    return getMongoClient().getDatabase(database).getCollection(tradeTickDepthCollectionName, TradeTickDepth.class);
  }
  protected MongoCollection<Currency> getCurrencyCollection() {
    return getMongoClient().getDatabase(database).getCollection(currencyCollectionName, Currency.class);
  }
  protected MongoCollection<ZerodhaInstrument> getZerodhaInstrumentsCollection() {
    return getMongoClient().getDatabase(database).getCollection(zerodhaInstrumentCollectionName, ZerodhaInstrument.class);
  }
  protected MongoCollection<DailyVIX> getDailyVixCollection() {
    return getMongoClient().getDatabase(database).getCollection(dailyVix, DailyVIX.class);
  }
  protected MongoCollection<SimulatedLongStraddle> getAllSimulatedTradesCollection() {
    return getMongoClient().getDatabase(database).getCollection(allSimulatedTradeName, SimulatedLongStraddle.class);
  }

  protected void popuateZerodhaInstrumentCollection() {
    OkHttpClient client = new OkHttpClient.Builder()
            .build();
    String url = "https://api.kite.trade/instruments";
    Map<String, String> extraHeaders = new HashMap<>();
    extraHeaders.put("X-Kite-Version", "3");
    Request request = new Request.Builder()
            .url(url)
            .headers(allHeaders(new Headers.Builder(), Headers.of(extraHeaders)).build())
            .get()
            .build();
    CSVFormat format = CSVFormat
            .DEFAULT
            .builder()
            .setSkipHeaderRecord(true)
            .setHeader("instrument_token", "exchange_token", "symbol", "name", "last_price", "expiry", "strike", "tick_size", "lot_size", "instrument_type", "segment", "exchange").build();

    List<ZerodhaInstrument> ZerodhaInstrument = new ArrayList<>();
    MongoCollection<ZerodhaInstrument> ZerodhaInstrumentCollection = getZerodhaInstrumentsCollection();
    ZerodhaInstrumentCollection.deleteMany(new Document());
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
    try (Response response = client.newCall(request).execute()) {
      InputStream is = response.body().byteStream();
      org.apache.commons.csv.CSVParser parser = org.apache.commons.csv.CSVParser.parse(is, Charset.defaultCharset(), format);
      try {

        parser.stream().forEach(record -> {
          try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2099, DECEMBER, 31, 23, 59, 59);
            Date expiry = calendar.getTime();
            String expiryStr = record.get("expiry").strip();
            if (!expiryStr.isBlank()) {

              try {

                expiry = Date.from(LocalDate.parse(expiryStr, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.of(15,30)).toInstant(OffsetDateTime.now().getOffset()));
              }
              catch (Exception e) {
                expiry = calendar.getTime();
              }
            }
            ZerodhaInstrument zi = new ZerodhaInstrument(record.get("instrument_token"),
                    record.get("exchange_token"),
                    record.get("symbol"),
                    record.get("name"),
                    expiry,
                    Float.parseFloat(record.get("strike")),
                    Float.parseFloat(record.get("tick_size")),
                    Long.parseLong(record.get("lot_size")),
                    record.get("instrument_type"),
                    ExchangeSegment.create(record.get("exchange"), record.get("segment")));
            ZerodhaInstrument.add(zi);
            if (ZerodhaInstrument.size() > 1000) {
              ZerodhaInstrumentCollection.insertMany(ZerodhaInstrument);
              ZerodhaInstrument.clear();
            }
          } catch (Exception e) {
            System.out.println(String.format("ERROR %s", record));
            e.printStackTrace();
          }
        });
      } catch (UncheckedIOException cex) {
        System.out.println(cex);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  protected void populateIndiaVix(Date from, Date to) throws IOException, ParseException {
    SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat ddmmmyyyy = new SimpleDateFormat("dd-MMM-yyyy");
    OkHttpClient client = new OkHttpClient.Builder()
            .build();
    String url = String.format("https://www.nseindia.com/api/historicalOR/vixhistory?from=%s&to=%s&csv=true", ddmmyyyy.format(from), ddmmyyyy.format(to));
    Map<String, String> extraHeaders = new HashMap<>();
    Request request = new Request.Builder()
            .url(url)
            .headers(allHeaders(new Headers.Builder(), Headers.of(extraHeaders)).build())
            .get()
            .build();
    List<DailyVIX> dailyVIXList = new ArrayList<>();
    try (Response response = client.newCall(request).execute()) {
      String body = response.body().string();
      System.out.println(response.code() + " " + body);
      Gson gson = new Gson();
      JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
      JsonArray array = jsonObject.getAsJsonArray("data");
      for (JsonElement je : array.asList()) {
        JsonObject jo = je.getAsJsonObject();
        Date timestamp = ddmmmyyyy.parse(jo.get("EOD_TIMESTAMP").getAsString());
        float open = jo.get("EOD_OPEN_INDEX_VAL").getAsFloat();
        float high = jo.get("EOD_HIGH_INDEX_VAL").getAsFloat();
        float low = jo.get("EOD_LOW_INDEX_VAL").getAsFloat();
        float close = jo.get("EOD_CLOSE_INDEX_VAL").getAsFloat();
        float prevClose = jo.get("EOD_PREV_CLOSE").getAsFloat();
        float change = jo.get("VIX_PTS_CHG").getAsFloat();
        float percentageChange = jo.get("VIX_PERC_CHG").getAsFloat();
        DailyVIX dv = new DailyVIX(UUID.randomUUID(), timestamp, open, high, low, close, prevClose, change, percentageChange);
        dailyVIXList.add(dv);
      }
    }
    InsertManyResult imr = getMongoClient().getDatabase(database).getCollection(dailyVix, DailyVIX.class).insertMany(dailyVIXList);
    System.out.println(imr.toString());
  }
  protected void populateIndiaVix(Date date) throws IOException, ParseException {
    populateIndiaVix(date, date);
  }
  protected void populateIndiaVix() throws ParseException {

    SimpleDateFormat ddmmmyyyy = new SimpleDateFormat("dd-MMM-yyyy");
    Date now = new Date();
    Date timestamp = ddmmmyyyy.parse(ddmmmyyyy.format(now));
  }

}
