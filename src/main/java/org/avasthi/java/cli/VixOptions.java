package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Quote;
import com.zerodhatech.models.Tick;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

public class VixOptions implements OptionsInterface, KiteTradingInterface.TickListener {
    private final String[] indexSymbols = { "NSE:INDIA VIX"};
    private final List<Bson> defaultFilters = List.of(Filters.eq("name", "NIFTY"),
            Filters.eq("es", "NSE_OPTIONS")
    );
    private final Bson vixFilter = Filters.and(
            Filters.eq("name", "INDIA VIX"),
            Filters.eq("symbol", "INDIA VIX"),
            Filters.eq("es", "NSE_INDICES")
    );

    private final MongoCollection<ZerodhaInstrument> zerodhaInstrumentCollection;
    private final KiteTradingInterface kiteTradingInterface;
    private final ZerodhaInstrument vixInstrument;
    private double lastPrice;
    public VixOptions(MongoCollection<ZerodhaInstrument> zerodhaInstrumentCollection,
                      KiteTradingInterface kiteTradingInterface) {
        this.zerodhaInstrumentCollection = zerodhaInstrumentCollection;
        this.kiteTradingInterface = kiteTradingInterface;
        this.vixInstrument = zerodhaInstrumentCollection.find(vixFilter).first();
        initializeQuote();
    }
    public List<Long> getTokensToSubscribe() {
        List<Long> tokensToSubscribe = List.of(Long.parseLong(vixInstrument.instrumentToken()));
        return tokensToSubscribe;
    }
    public long getToken() {
        return Long.parseLong(vixInstrument.instrumentToken());
    }

    @Override
    public void subscribe() {
        kiteTradingInterface.addListener(List.of(Long.parseLong(vixInstrument.instrumentToken())), this);
    }

    @Override
    public void onTick(Tick tick) {
        lastPrice = tick.getLastTradedPrice();
    }

    @Override
    public void onBatchComplete(List<Tick> batch) {

    }

    private void initializeQuote() {

        try {
            Map<String, Quote> quotes = kiteTradingInterface.getKiteConnect().getQuote(indexSymbols);
            for (Map.Entry<String, Quote> e : quotes.entrySet()) {
                lastPrice = e.getValue().lastPrice;
                System.out.println(e.getKey() + " + " + e.getValue().lastPrice);

            }
        } catch (KiteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Set mode (Full, Quote, or LTP)
    }
    public double getLastPrice() {
        if (lastPrice == 0) {
            initializeQuote();
        }
        return lastPrice;
    }
}
