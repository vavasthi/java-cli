package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Quote;
import com.zerodhatech.models.Tick;
import org.avasthi.java.cli.pojos.OptionPair;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NiftyOptions implements OptionsInterface, KiteTradingInterface.TickListener{
    private final String asset = "NIFTY OPTIONS";
    private final String[] indexSymbols = { "NSE:NIFTY 50"};
    private final int minimumDaysToExpiry = 4;
    private final List<Bson> defaultFilters = List.of(Filters.eq("name", "NIFTY"),
            Filters.eq("es", "NSE_OPTIONS")
    );
    private final Bson nifty50Filter = Filters.and(
            Filters.eq("name", "NIFTY 50"),
            Filters.eq("symbol", "NIFTY 50"),
            Filters.eq("es", "NSE_INDICES")
    );
    private double lastPrice;
    private final MongoCollection<ZerodhaInstrument> zerodhaInstrumentCollection;
    private final KiteTradingInterface kiteTradingInterface;
    private final ZerodhaInstrument niftyInstrument;
    private Set<ZerodhaInstrument> allOptions = new HashSet<>();
    private final Map<Float, ZerodhaInstrument> callOptions = new HashMap<>();
    private final Map<Float, ZerodhaInstrument> putOptions = new HashMap<>();
    public NiftyOptions(MongoCollection<ZerodhaInstrument> zerodhaInstrumentCollection,
                        KiteTradingInterface kiteTradingInterface) {
        this.zerodhaInstrumentCollection = zerodhaInstrumentCollection;
        this.kiteTradingInterface = kiteTradingInterface;
        this.niftyInstrument = zerodhaInstrumentCollection.find(nifty50Filter).first();
        loadRelaventInstruments(zerodhaInstrumentCollection);
        initializeQuote();
    }
    public void loadRelaventInstruments(MongoCollection<ZerodhaInstrument> zerodhaInstrumentCollection) {
        Date expiry = getExpiryDate(zerodhaInstrumentCollection);
        zerodhaInstrumentCollection.find(Filters.and(Stream.concat(defaultFilters.stream(), Stream.of(Filters.eq("expiry", expiry))).toList()))
                .forEach(zi -> {
                    allOptions.add(zi);
            if (zi.instrumentType().equals("CE")) {
                callOptions.put(zi.strike(), zi);
            }
            else if (zi.instrumentType().equals("PE")) {
                putOptions.put(zi.strike(), zi);
            }
        });
    }
    private Date getExpiryDate(MongoCollection<ZerodhaInstrument> zerodhaInstrumentMongoCollection){

        /**
         * We return the first date that meets our minimumDaysToExpiry criteria. We want the closest options
         * but not too close.
         */
        List<Date> expiryDates = zerodhaInstrumentMongoCollection.distinct("expiry",
                Filters.and(Filters.eq("name", "NIFTY"), Filters.eq("es", "NSE_OPTIONS")), Date.class).into(new ArrayList<>());
        expiryDates.sort(new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return o1.before(o2) ? -1 : o2.before(o1) ? 1 : 0;
            }
        });

        LocalDateTime now = LocalDateTime.now();
        for (Date date : expiryDates) {
            LocalDateTime expiry = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            long daysBetween = ChronoUnit.DAYS.between(now, expiry);
            if (daysBetween >= minimumDaysToExpiry) {
                return date;
            }
        }
        return null;
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
    public OptionPair getOptionPair(float strike) {
        return new OptionPair(callOptions.get(strike), putOptions.get(strike));

    }
    public List<Long> getTokensToSubscribe() {
        List<Long> tokensToSubscribe = new ArrayList<>();
        callOptions.forEach((strike, zi) -> {
            tokensToSubscribe.add(Long.parseLong(zi.instrumentToken()));
        });
        putOptions.forEach((strike, zi) -> {
            tokensToSubscribe.add(Long.parseLong(zi.instrumentToken()));
        });
        return tokensToSubscribe;
    }
    public long getToken() {
        return Long.parseLong(niftyInstrument.instrumentToken());
    }

    @Override
    public void subscribe() {
        kiteTradingInterface.addListener(List.of(Long.parseLong(niftyInstrument.instrumentToken())), this);
    }

    @Override
    public void onTick(Tick tick) {
        lastPrice = tick.getLastTradedPrice();
    }

    @Override
    public void onBatchComplete(List<Tick> batch) {

    }

    public double getLastPrice() {
        if (lastPrice == 0) {
            initializeQuote();
        }
        return lastPrice;
    }
    @Override
    public Set<ZerodhaInstrument> getallOptions() {
        return allOptions;
    }
    @Override
    public Set<Float> findNearestStrike(double spotPrice) {
        int expiryBoundaries = 50;
        float lowerExpiry = (long) Math.floor(spotPrice /expiryBoundaries) * 50;
        float upperExpiry = lowerExpiry + expiryBoundaries;
        return Arrays.asList(lowerExpiry, upperExpiry).stream().collect(Collectors.toSet());
    }

    public String getAsset() {
        return asset;
    }
}
