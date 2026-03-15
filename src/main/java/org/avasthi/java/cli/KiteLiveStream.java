package org.avasthi.java.cli;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Quote;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnTicks;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.csv.CSVFormat;
import org.avasthi.java.cli.pojos.ExchangeSegment;
import org.avasthi.java.cli.pojos.StockMaster;
import org.avasthi.java.cli.pojos.ZerodhaInstruments;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Calendar.YEAR;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

record IV(double call, double put) {

}
public class KiteLiveStream {

    private final String mongoUrl = "mongodb://localhost";
    private final String database = "capitalMarkets";
    private final MongoClient mongoClient = getMongoClient();
    private final String stockMasterCollectionName = "stockMaster";
    private final String stockPriceCollectionName = "stockPrice";
    private final String optionPriceCollectionName = "optionPrice";
    private final String indexPriceCollectionName = "indexPrice";
    private final String corporateEventCollectionName = "corporateEvents";
    private final String minuteTickCollectionName = "minuteTick";
    private final String tradeTickCollectionName = "tradeTick";
    private final String tradeTickDepthCollectionName = "tradeTickDepth";
    private final String quarterlyResultsCollectionName = "quarterlyResults";
    private final String zerodhaInstrumentsCollectionName = "zerodhaInstruments";
    private final String cpiCollectionName = "cpi";
    private final String iipCollectionName = "iip";
    private final String currencyCollectionName = "currency";

    private final MongoCollection<Tick> tickCollection = getMongoClient().getDatabase("newCapitalMarkets").getCollection("tick", Tick.class);
    private final Map<String, Long> indicesSymbol = Map.of(
            "NIFTY", 256265L
    );
    private final Map<Long, Double> lastTradedPrice = new HashMap<>();
    private final double riskFreeRate = 0.10;

    public static void main(String[] args) throws IOException, KiteException {
        // 1. Initialize KiteConnect with your credentials
        KiteLiveStream kiteLiveStream = new KiteLiveStream();
        kiteLiveStream.initialize();
    }
    private void initialize() throws KiteException, IOException {
        String apiKey = "y57gy37ydalmh6ky";
        String accessToken = getAccessToken(apiKey);
        KiteConnect kiteConnect = new KiteConnect(apiKey);
        kiteConnect.setAccessToken(accessToken);
        popuateZerodhaInstrumentsCollection();


        // 2. Initialize KiteTicker
        KiteTicker tickerProvider = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());

        // 3. Set Up Listeners
        tickerProvider.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                /** * Subscribe to instrument tokens.
                 * Example: 256265 (INFY), 738561 (RELIANCE)
                 */
                ArrayList<Long> tokens = new ArrayList<>();

                Map<String, Quote> quotes = null;
                double lastPrice = 24000;
                try {
                    quotes = kiteConnect.getQuote(new String[]{"NSE:NIFTY 50", "NSE:INDIA VIX"});
                    for (Map.Entry<String, Quote> e : quotes.entrySet()) {
                        System.out.println(e.getKey() + " " + e.getValue().lastPrice);
                        lastPrice = e.getValue().lastPrice;
                    }

                } catch (KiteException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Bson query = Filters.and(
                        Filters.eq("expiry", LocalDateTime.of(2026,3,17,15,30).toInstant(OffsetDateTime.now().getOffset())),
                        Filters.lt("strike", lastPrice + 300),
                        Filters.gt("strike", lastPrice - 300)
                );
                tokens.add(256265L);
                tokens.add(264969L);
                System.out.println(query);
                MongoCursor<ZerodhaInstruments> cursor = getZerodhaInstrumentsCollection().find(query).cursor();
                while (cursor.hasNext()) {
                    ZerodhaInstruments zi = cursor.next();
                    tokens.add(Long.parseLong(zi.instrumentToken()));
                }

                tickerProvider.subscribe(tokens);

                // Set mode (Full, Quote, or LTP)
                tickerProvider.setMode(tokens, KiteTicker.modeFull);
                System.out.println("Connected and Subscribed!");
            }
        });

        tickerProvider.setOnTickerArrivalListener(new OnTicks() {
            MongoCollection<ZerodhaInstruments> zerodhaInstrumentsMongoCollection
                    = getZerodhaInstrumentsCollection();
            MongoCollection<StockMaster> stockMasterMongoCollection
                    = getStockMasterCollection();
            @Override
            public void onTicks(ArrayList<Tick> ticks) {
                List<String> stringList = new ArrayList<>();
                for (Tick tick : ticks) {
                    ZerodhaInstruments zerodhaInstruments
                            = zerodhaInstrumentsMongoCollection.find(Filters.eq("instrumentToken", String.valueOf(tick.getInstrumentToken()))).first();
                    if (zerodhaInstruments != null) {
                        double spotPrice = lastTradedPrice.get(indicesSymbol.get(zerodhaInstruments.name()));
                        double distanceFromSpot =Math.abs(spotPrice - ((double)zerodhaInstruments.strike()));

                        if (distanceFromSpot < 100) {

                            double iv = getImpliedVolatility(tick, zerodhaInstruments, spotPrice, riskFreeRate);
                            System.out.println("Token: " + tick.getInstrumentToken() +
                                    " | Timestamp: " + tick.getLastTradedTime() +
                                    " | LTP: " + tick.getLastTradedPrice() +
                                    " | SYMBOL: " + zerodhaInstruments.symbol() +
                                    " | NAME: " + zerodhaInstruments.name() +
                                    " | EXPIRY: " + zerodhaInstruments.expiry() +
                                    " | STRIKE: " + zerodhaInstruments.strike() +
                                    " | IV: " + iv +
                                    " | Volume: " + tick.getVolumeTradedToday()
                            );
                            stringList.add(String.format("%s,%s,%.2f,%s,%s,%s,%.2f,%f,%d\n", tick.getInstrumentToken(), tick.getLastTradedTime(), tick.getLastTradedPrice(), zerodhaInstruments.symbol(), zerodhaInstruments.name(), zerodhaInstruments.expiry(), zerodhaInstruments.strike(), iv, tick.getVolumeTradedToday()));
                        }
                    }
                    else {
                        lastTradedPrice.put(tick.getInstrumentToken(), tick.getLastTradedPrice());
                        System.out.println("Token: " + tick.getInstrumentToken() +
                                " | LTP: " + tick.getLastTradedPrice() +
                                " | Volume: " + tick.getVolumeTradedToday() );
                    }
                }
                StringBuffer content = new StringBuffer();
                File path = new File("data.csv");
                try (BufferedWriter writer = Files.newBufferedWriter(
                        path.toPath(),
                        StandardOpenOption.CREATE, // Create the file if it doesn't exist
                        StandardOpenOption.APPEND   // Open in append mode
                )) {
                    for (String line : stringList) {

                        writer.write(line);
                    }
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }
                stringList.clear();
            }
        });

        tickerProvider.setOnDisconnectedListener(new OnDisconnect() {
            @Override
            public void onDisconnected() {
                System.out.println("Connection Closed.");
            }
        });

        // 4. Connect to WebSocket
        tickerProvider.connect();

        /**
         * Note: The ticker runs in a separate thread.
         * Use tickerProvider.setTryReconnection(true) to handle auto-reconnects.
         */
        tickerProvider.setTryReconnection(true);
        tickerProvider.setMaximumRetries(10);
        tickerProvider.setMaximumRetryInterval(30);
    }
    public static String getAccessToken(String apiKey) throws KiteException {
        OkHttpClient client =  new OkHttpClient();
        String url = String.format("https://us-central1-algo-trading-490a9.cloudfunctions.net/accessToken?api_key=%s", apiKey);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(response.body().string(), JsonObject.class);
            return object.get("access_token").getAsString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
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
    private MongoCollection<ZerodhaInstruments> getZerodhaInstrumentsCollection() {
        return getMongoClient().getDatabase(database).getCollection(zerodhaInstrumentsCollectionName, ZerodhaInstruments.class);
    }
    private MongoCollection<StockMaster> getStockMasterCollection() {
        return getMongoClient().getDatabase(database).getCollection(stockMasterCollectionName, StockMaster.class);
    }
    private double getImpliedVolatility(Tick tick, ZerodhaInstruments zerodhaInstruments, double spotPrice, double riskFreeRate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(zerodhaInstruments.expiry());
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        double timeToExpirationInYear = getTimeToExpiryInYears(calendar.getTime());
        return ImpliedVolatility.calculateIV(tick.getLastTradedPrice(), spotPrice, zerodhaInstruments.strike(), timeToExpirationInYear, riskFreeRate, zerodhaInstruments.symbol().endsWith("CE"));
    }
    private static double getTimeToExpiryInYears(Date expiryDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expiryDate);
        calendar.set(calendar.get(YEAR), Calendar.JANUARY, 1, 0 ,0, 0);
        LocalDateTime expiryDay = LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime startOfTheYear = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
        calendar.add(YEAR, 1);
        calendar.add(Calendar.SECOND, -1);
        LocalDateTime endOfTheYear = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());

        long secondsToExpiry = ChronoUnit.SECONDS.between(LocalDateTime.now(ZoneId.systemDefault()), expiryDay);
        long secondsInAYear = ChronoUnit.SECONDS.between(startOfTheYear, endOfTheYear);
        return (double)secondsToExpiry  / (double) secondsInAYear;

    }


    public void popuateZerodhaInstrumentsCollection() {
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

        List<ZerodhaInstruments> zerodhaInstruments = new ArrayList<>();
        MongoCollection<ZerodhaInstruments> zerodhaInstrumentsCollection = getZerodhaInstrumentsCollection();
        zerodhaInstrumentsCollection.deleteMany(new Document());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        try (Response response = client.newCall(request).execute()) {
            InputStream is = response.body().byteStream();
            org.apache.commons.csv.CSVParser parser = org.apache.commons.csv.CSVParser.parse(is, Charset.defaultCharset(), format);
            try {

                parser.stream().forEach(record -> {
                    try {
                        Date expiry = Date.from(LocalDate.parse(record.get("expiry"), DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.of(15,30)).toInstant(OffsetDateTime.now().getOffset()));
                        ZerodhaInstruments zi = new ZerodhaInstruments(record.get("instrument_token"),
                                record.get("exchange_token"),
                                record.get("symbol"),
                                record.get("name"),
                                expiry,
                                Long.parseLong(record.get("strike")),
                                Float.parseFloat(record.get("tick_size")),
                                Long.parseLong(record.get("lot_size")),
                                record.get("instrument_type"),
                                ExchangeSegment.create(record.get("exchange"), record.get("segment")));
                        zerodhaInstruments.add(zi);
                        if (zerodhaInstruments.size() > 1000) {
                            zerodhaInstrumentsCollection.insertMany(zerodhaInstruments);
                            zerodhaInstruments.clear();
                        }
                    } catch (Exception e) {
//                        System.out.println(String.format("ERROR %s", record));
                    }
                });
            } catch (UncheckedIOException cex) {
                System.out.println(cex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Headers.Builder defaultHeaders(Headers.Builder builder) {

        builder.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
        return builder;
    }

    private Headers.Builder allHeaders(Headers.Builder builder,
                                       Headers headers) {
        builder = defaultHeaders(builder);
        for (Iterator<Pair<String, String>> it = headers.iterator(); it.hasNext(); ) {
            Pair<String, String> kv = it.next();
            builder.add(kv.getFirst(), kv.getSecond());
        }
        return builder;
    }

}