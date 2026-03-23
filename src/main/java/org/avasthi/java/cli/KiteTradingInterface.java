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
import com.zerodhatech.models.Quote;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnTicks;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.avasthi.java.cli.pojos.StockMaster;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

record IV(double call, double put) {

}
public class KiteTradingInterface extends Base {

    public interface TickListener {
        void onTick(Tick tick);
        void onBatchComplete(List<Tick> batch);
    }

    private final MongoCollection<ZerodhaInstrument> zerodhaInstrumentCollection
            = getMongoClient().getDatabase(database).getCollection(zerodhaInstrumentCollectionName, ZerodhaInstrument.class);
    private final String[] indexSymbols = { "NIFTY", "INDIA VIX"};
    private final Map<String, Long> indicesSymbol = Map.of(
            "NIFTY", 256265L
    );
    private final Map<Long, Double> lastTradedPrice = new HashMap<>();
    private final double riskFreeRate = 0.10;

    private final Map<Long, Set<TickListener>> tickListeners = new HashMap<>();
    private final Map<TickListener, Set<Long>> reverseTickListeners = new HashMap<>();
    private final KiteTicker tickerProvider;
    private final String apiKey;
    private final String accessToken;
    private final KiteConnect kiteConnect;

    private final String nifty = "NSE:NIFTY 50";
    private final String vix = "NSE:INDIA VIX";

    private final Map<String, Double> indexValues = new HashMap<>();

    private final Map<String, ZerodhaInstrument> indexInstruments = new HashMap<>();
    private Thread tickerThread;
    public KiteTradingInterface(String apiKey) throws KiteException {

        //apiKey = "y57gy37ydalmh6ky";
        this.apiKey = apiKey;
        this.accessToken = getAccessToken(apiKey);
        this.kiteConnect = new KiteConnect(apiKey);
        kiteConnect.setAccessToken(accessToken);
       // popuateZerodhaInstrumentCollection();
        // 2. Initialize KiteTicker
        tickerProvider = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());
        getMongoClient()
                .getDatabase(database)
                .getCollection(zerodhaInstrumentCollectionName, ZerodhaInstrument.class)
                .find(Filters.in("symbol", indexSymbols)).forEach( zi -> {
            indexInstruments.put(zi.symbol(), zi);
        });
    }
    public void initialize() throws KiteException, IOException {

       tickerProvider.setOnConnectedListener(new OnConnect() {
            @Override
            public void onConnected() {
                try {
                    Map<String, Quote> quotes = kiteConnect.getQuote(indexSymbols);
                    for (Map.Entry<String, Quote> e : quotes.entrySet()) {
                        indexValues.put(e.getKey(), e.getValue().lastPrice);
                    }
                } catch (KiteException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ArrayList<Long> tokens = indexInstruments.values().stream().map(zi -> Long.parseLong(zi.instrumentToken())).collect(Collectors.toCollection(ArrayList::new));
                tickerProvider.subscribe(tokens);

                // Set mode (Full, Quote, or LTP)
                tickerProvider.setMode(tokens, KiteTicker.modeFull);
                System.out.println("Connected and Subscribed!");
            }
        });

        tickerProvider.setOnTickerArrivalListener(new OnTicks() {
            MongoCollection<ZerodhaInstrument> ZerodhaInstrumentMongoCollection
                    = getZerodhaInstrumentsCollection();
            MongoCollection<StockMaster> stockMasterMongoCollection
                    = getStockMasterCollection();
            @Override
            public void onTicks(ArrayList<Tick> ticks) {
                List<String> stringList = new ArrayList<>();
                for (Tick tick : ticks) {
                    Set<TickListener> listeners = tickListeners.get(tick.getInstrumentToken());
                    if (listeners != null) {
                        for (TickListener listener : listeners) {
                            listener.onTick(tick);
                        }
                    }
                }
                reverseTickListeners.keySet().forEach(listener-> listener.onBatchComplete(ticks));
            }
        });

        tickerProvider.setOnDisconnectedListener(new OnDisconnect() {
            @Override
            public void onDisconnected() {
                System.out.println("Connection Closed.");
            }
        });

    }
    public void start() throws KiteException {

        if (tickerThread != null) {
            tickerThread.interrupt();
        }
        tickerThread = new Thread(() -> {

            // 4. Connect to WebSocket
            tickerProvider.connect();

            /**
             * Note: The ticker runs in a separate thread.
             * Use tickerProvider.setTryReconnection(true) to handle auto-reconnects.
             */
            tickerProvider.setTryReconnection(true);
            try {
                tickerProvider.setMaximumRetries(10);
                tickerProvider.setMaximumRetryInterval(30);
            } catch (KiteException e) {
                throw new RuntimeException(e);
            }
        });
        tickerThread.start();

    }
    public void stop() throws InterruptedException {
        if (tickerThread != null && tickerThread.isAlive()) {
            tickerThread.interrupt();
        }
        tickerThread.join();
    }
    public void join() throws InterruptedException {
        if (tickerThread != null && tickerThread.isAlive()) {
            tickerThread.join();
        }
    }
    public void finish() throws KiteException {
        tickerProvider.disconnect();
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
    private double getImpliedVolatility(Tick tick, ZerodhaInstrument ZerodhaInstrument, double spotPrice, double riskFreeRate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ZerodhaInstrument.expiry());
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        double timeToExpirationInYear = getTimeToExpiryInYears(calendar.getTime());
        return ImpliedVolatility.calculateIV(tick.getLastTradedPrice(), spotPrice, ZerodhaInstrument.strike(), timeToExpirationInYear, riskFreeRate, ZerodhaInstrument.symbol().endsWith("CE"));
    }

    public synchronized void addListener(List<Long> instrumentTokens, TickListener tickListener) {
        instrumentTokens.forEach(instrumentToken -> {
            Set<TickListener> listeners = tickListeners.get(instrumentToken);
            if (listeners == null) {
                listeners = new HashSet<>();
                tickListeners.put(instrumentToken, listeners);
            }
            listeners.add(tickListener);
            Set<Long> tokenList = reverseTickListeners.get(tickListener);
            if (tokenList == null) {
                tokenList = new HashSet<>();
                reverseTickListeners.put(tickListener, tokenList);
            }
            tokenList.add(instrumentToken);
        });
        ArrayList<Long> tokensToSubscribe = instrumentTokens.stream().collect(Collectors.toCollection(ArrayList::new));
        tickerProvider.subscribe(tokensToSubscribe);
        tickerProvider.setMode(tokensToSubscribe, KiteTicker.modeFull);

    }
    public synchronized void removeListener(TickListener tickListener) {
        Set<Long> instrumentTokens = reverseTickListeners.remove(tickListener);
        ArrayList<Long> instrumentsToBeUnsubscribed = new ArrayList<>();
        instrumentTokens.forEach(instrumentToken -> {
            Set<TickListener> listeners = tickListeners.remove(instrumentToken);
            listeners.remove(tickListener);
            if (!listeners.isEmpty()) {
                tickListeners.put(instrumentToken, listeners);
            }
            else {
                instrumentsToBeUnsubscribed.add(instrumentToken);
            }
        });
        if (!instrumentsToBeUnsubscribed.isEmpty()) {

            tickerProvider.unsubscribe(instrumentsToBeUnsubscribed);
        }
    }
    public KiteConnect getKiteConnect() {
        return kiteConnect;
    }
}