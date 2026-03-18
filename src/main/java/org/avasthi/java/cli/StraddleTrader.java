package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Tick;
import org.avasthi.java.cli.pojos.LongStraddle;
import org.avasthi.java.cli.pojos.OptionPair;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;

import java.io.IOException;
import java.util.*;

public class StraddleTrader extends Base implements KiteTradingInterface.TickListener{

    private final double maxDiffInSpotAndStrike = 2.0;
    private final double riskFreeRate = .10;
    private final long defaultQuantity = 1;
    private final double profitPercentage = .03;
    private final double lossPercentage = .01;
    private final MongoCollection<ZerodhaInstrument> zerodhaInstrumentCollection
            = getMongoClient().getDatabase(database).getCollection(zerodhaInstrumentCollectionName, ZerodhaInstrument.class);
    private final List<OptionsInterface> options = new ArrayList<>();
    private final KiteTradingInterface kiteTradingInterface;
    private final OptionsInterface vixOptions;
    private final OptionsInterface niftyOptions;
    private final Map<ZerodhaInstrument, Tick> ticks = new HashMap<>();
    private final HashMap<Long, ZerodhaInstrument> allOptions = new HashMap<>();
    private final TradeBook tradeBook = new TradeBook(100000);
    public static void main(String[] args) throws IOException, KiteException, InterruptedException {

        StraddleTrader straddleTrader = new StraddleTrader();
        straddleTrader.start();
        straddleTrader.setupInitialSubscriptions();
    }

    private StraddleTrader() throws KiteException {
        kiteTradingInterface =  new KiteTradingInterface("y57gy37ydalmh6ky");
        niftyOptions = new NiftyOptions(zerodhaInstrumentCollection, kiteTradingInterface);
        vixOptions = new VixOptions(zerodhaInstrumentCollection, kiteTradingInterface);
        options.addAll(List.of(niftyOptions, vixOptions));
        niftyOptions.getallOptions().forEach(zi -> allOptions.put(Long.parseLong(zi.instrumentToken()), zi));
    }
    @Override
    public void onTick(Tick tick) {
        double spotPrice = niftyOptions.getLastPrice();
        ticks.put(allOptions.get(tick.getInstrumentToken()), tick);
    }

    @Override
    public void onBatchComplete(List<Tick> batch) {
        System.out.println(ticks.size() + " batch completed");
        processNiftyTrade();
    }

    private void start() throws KiteException, InterruptedException, IOException {
        kiteTradingInterface.initialize();
        kiteTradingInterface.start();
        Thread.sleep(5000);
        options.forEach(option -> {
            System.out.println(option.getLastPrice());});
        kiteTradingInterface.join();
    }
    private void setupInitialSubscriptions() throws IOException, KiteException {
        List<Long> instrumentTokens = new ArrayList<>();
        options.forEach(oi -> {
            instrumentTokens.addAll(oi.getTokensToSubscribe());
        });
        kiteTradingInterface.addListener(instrumentTokens, this);
        options.forEach(o -> o.subscribe());
    }

    private void processNiftyTrade() {
        double spotPrice = niftyOptions.getLastPrice();
        double vix = vixOptions.getLastPrice();
        Set<Float> strikes = niftyOptions.findNearestStrike(spotPrice);
        for (Float strike : strikes) {
            OptionPair optionPair = niftyOptions.getOptionPair(strike);
            Tick callTick = ticks.get(optionPair.call());
            Tick putTick = ticks.get(optionPair.put());
            double callIv = ImpliedVolatility.calculateIV(callTick.getLastTradedPrice(), spotPrice, strike, getTimeToExpiryInYears(optionPair.call().expiry()), riskFreeRate, true);
            double putIv = ImpliedVolatility.calculateIV(putTick.getLastTradedPrice(), spotPrice, strike, getTimeToExpiryInYears(optionPair.put().expiry()), riskFreeRate, false);
            LongStraddle longStraddle = tradeBook.get(strike);
            if (longStraddle == null) {
                /**
                 * No trade exists for this strike price. Check if we have liquidity and trade conditions are met
                 */
                if (isTradeAcceptable(strike, spotPrice, vix, callTick, putTick, callIv, putIv)) {

                    tradeBook.buy(niftyOptions.getAsset(), optionPair.call().symbol(), optionPair.put().symbol(), callTick, putTick, vix, defaultQuantity, spotPrice, strike, callIv, putIv);
                }

            }
            else {
                /**
                 * Square the position if sufficient profits are being made.
                 */
                tradeBook.squareIfProfitableOrStopLoss(strike, spotPrice, callTick, putTick, callIv, putIv, profitPercentage, lossPercentage);
            }
        }

    }

    private boolean isTradeAcceptable(double strike, double spotPrice, double vix, Tick callTick, Tick putTick, double callIv, double putIv) {
        /**
         * Here are our rules to take a new trade..
         *  1. India VIX is less than 22.5
         *  2. Spot price is within a maxDiffInSpotAndStrike of strike price
         *  3. Option IV is within 15% of India VIX
         *  4. Call IV and Put IV are within 15%
         */
        vix = vix / 100;
        if (vix < .225) {
            if (Math.abs(spotPrice - strike) < maxDiffInSpotAndStrike) {
                if (Math.max(Math.abs(vix - callIv), Math.abs(vix - putIv))/vix < .15) {
                    if (Math.abs(callIv - putIv)/callIv < .15) {
                        /**
                         * All conditions passed. We can take a trade..
                         */
                        return true;
                    }
                    else {
                        System.out.println(String.format("Call IV %.2f and put IV %.2f are more than 15% apart from each other", callIv, putIv));
                    }
                }
                else {
                    System.out.println(String.format("Call IV %.2f and put IV %.2f are more than 15% apart from India VIX %.2f", callIv, putIv, vix));

                }
            }
            else {
                System.out.println(String.format("Spot price %.2f is too far off the strike price %.2f", spotPrice, strike));
            }
        }
        else {
            System.out.println(String.format("India VIX %.2f is more than 22.5", vix));
        }
        return false;
    }
}
