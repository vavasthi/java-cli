package org.avasthi.java.cli;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.avasthi.java.cli.pojos.CorporateEvent;
import org.avasthi.java.cli.pojos.Currency;
import org.avasthi.java.cli.pojos.StockMaster;
import org.avasthi.java.cli.pojos.StockPrice;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.util.*;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Base {
    protected final String mongoUrl = "mongodb://localhost";
    protected final String database = "capitalMarkets";
    protected final MongoClient mongoClient = getMongoClient();
    protected final String stockMasterCollectionName = "stockMaster";
    protected final String stockPriceCollectionName = "stockPrice";
    protected final String corporateEventCollectionName = "corporateEvents";
    protected final String quarterlyResultsCollectionName = "quarterlyResults";
    protected final String cpiCollectionName = "cpi";
    protected final String iipCollectionName = "iip";
  protected final String currencyCollectionName = "currency";

    protected MongoClient getMongoClient() {
        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        return MongoClients.create(
                MongoClientSettings.builder().applyConnectionString(new ConnectionString(mongoUrl))
                        .codecRegistry(pojoCodecRegistry)
                        .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                        .build()
        );
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

        builder.add("Accept-Encoding", "gzip, deflate, br, zstd");
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
  protected MongoCollection<CorporateEvent> getCorporateEventsCollection() {
    return getMongoClient().getDatabase(database).getCollection(corporateEventCollectionName, CorporateEvent.class);
  }
  protected MongoCollection<StockMaster> getStockMasterCollection() {
    return getMongoClient().getDatabase(database).getCollection(stockMasterCollectionName, StockMaster.class);
  }
  protected MongoCollection<Currency> getCurrencyCollection() {
    return getMongoClient().getDatabase(database).getCollection(currencyCollectionName, Currency.class);
  }


}
