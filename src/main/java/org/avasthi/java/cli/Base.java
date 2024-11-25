package org.avasthi.java.cli;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.UuidRepresentation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Base {
    protected final String mongoUrl = "mongodb://localhost";
    protected final String database = "capitalMarkets";
    protected final MongoClient mongoClient = getMongoClient();
    protected final String stockMasterCollection = "stockMaster";
    protected final String stockPriceCollection = "stockPrice";
    protected final String corporateEventCollection = "corporateEvents";

    protected MongoClient getMongoClient() {
        return MongoClients.create(
                MongoClientSettings.builder().applyConnectionString(new ConnectionString(mongoUrl))
                        .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                        .build()
        );
    }
    protected WebDriver getWebDriver() {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing_for_trusted_sources_enabled", false);
        prefs.put("safebrowsing.enabled",false);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox", "disable-search-engine-choice-screen");
        chromeOptions.setAcceptInsecureCerts(true);
        chromeOptions.setExperimentalOption("prefs", prefs);
        return new ChromeDriver(chromeOptions);
    }
    protected Headers allReports(OkHttpClient client) {
        String url = "https://www.nseindia.com/all-reports";
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


}
