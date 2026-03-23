package org.avasthi.java.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zerodhatech.models.Tick;
import org.avasthi.java.cli.pojos.LongStraddle;
import org.avasthi.java.cli.pojos.SimulatedLongStraddle;
import org.avasthi.java.cli.pojos.SimulatedTrade;
import org.avasthi.java.cli.pojos.Trade;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeBook {
    private final Map<Float, LongStraddle> book = new HashMap<>();
    private double budget;
    private double currentPosition;
    private final PrintWriter writer;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    ;
    public TradeBook(double budget) throws IOException {
        this.budget = budget;
        this.currentPosition = 0;
        this.writer = new PrintWriter(new FileWriter("dump.txt", true));
    }
    public LongStraddle get(Float strike) {
        return book.get(strike);
    }

    public void buy(String asset,
                    String callSymbol,
                    String putSymbol,
                    Tick callTick,
                    Tick putTick,
                    double vix,
                    long quantity,
                    double spotPrice,
                    float strike,
                    double callIV,
                    double putIV) {
        double cost = quantity * (callTick.getLastTradedPrice() + putTick.getLastTradedPrice());
        if ((currentPosition + cost)*1.1 <= budget) {

            LongStraddle newTrade = LongStraddle.builder()
                    .tradeId(UUID.randomUUID())
                    .asset(asset)
                    .buy(new LongStraddle.Straddle(
                            Trade.builder()
                                    .timestamp(callTick.getTickTimestamp())
                                    .tradeType(Trade.TradeType.BUY)
                                    .symbol(callSymbol)
                                    .quantity(quantity)
                                    .premium(callTick.getLastTradedPrice())
                                    .volumeTraded(callTick.getLastTradedQuantity())
                                    .openInterestHigh(callTick.getOpenInterestDayHigh())
                                    .openInterestLow(callTick.getOpenInterestDayLow())
                                    .spotPrice(spotPrice)
                                    .IV(callIV)
                                    .build(),
                            Trade.builder()
                                    .timestamp(putTick.getTickTimestamp())
                                    .tradeType(Trade.TradeType.BUY)
                                    .symbol(putSymbol)
                                    .quantity(quantity)
                                    .premium(putTick.getLastTradedPrice())
                                    .volumeTraded(putTick.getLastTradedQuantity())
                                    .openInterestHigh(putTick.getOpenInterestDayHigh())
                                    .openInterestLow(putTick.getOpenInterestDayLow())
                                    .spotPrice(spotPrice)
                                    .IV(putIV)
                                    .build()
                    ))
                    .profitOpportunityCount(0)
                    .maxLoss(0)
                    .maxProfit(0)
                    .timestamp(callTick.getTickTimestamp())
                    .strike(strike)
                    .spotPrice(spotPrice)
                    .vix(vix)
                    .build();
            book.put(newTrade.getStrike(),  newTrade);
            currentPosition += cost;
            writer.println(String.format("BUYING Straddle\n%s,", gson.toJson(newTrade)));
        }
        writer.flush();
    }

    public void squareIfProfitableOrStopLoss(Float strike,
                                             double spotPrice,
                                             Tick callTick,
                                             Tick putTick,
                                             double callIV,
                                             double putIV,
                                             double profitPercentage,
                                             double lossPercentage,
                                             double accetableIvIncrease) {
        LongStraddle position = book.get(strike);
        double currentValue = (callTick.getAverageTradePrice()*position.getBuy().call().getQuantity() + putTick.getLastTradedPrice() * position.getBuy().put().getQuantity());
        double cost = position.getCost();
        double difference = currentValue - cost;
        position.setCurrentProfit(difference);
        if (difference > cost * profitPercentage ) {
            /**
             * Book profit
             */
            LongStraddle.Straddle sell = new LongStraddle
                    .Straddle(
                    Trade.builder()
                            .timestamp(callTick.getLastTradedTime())
                            .tradeType(Trade.TradeType.SELL)
                            .symbol(position.getBuy().call().getSymbol())
                            .quantity(position.getBuy().call().getQuantity())
                            .premium(callTick.getLastTradedPrice())
                            .volumeTraded(callTick.getVolumeTradedToday())
                            .openInterestHigh(callTick.getOpenInterestDayHigh())
                            .spotPrice(spotPrice)
                            .IV(callIV)
                            .build(),
                    Trade.builder()
                            .timestamp(putTick.getLastTradedTime())
                            .tradeType(Trade.TradeType.SELL)
                            .symbol(position.getBuy().put().getSymbol())
                            .quantity(position.getBuy().put().getQuantity())
                            .premium(putTick.getLastTradedPrice())
                            .volumeTraded(putTick.getVolumeTradedToday())
                            .openInterestHigh(putTick.getOpenInterestDayHigh())
                            .spotPrice(spotPrice)
                            .IV(callIV)
                            .build());
            position.setSell(sell);
            position.setMaxProfit(difference);
            position.setCurrentProfit(difference);
            currentPosition -= currentValue;
            /**
             * Here we make actual call to broker..
             */
            book.remove(strike);
            writer.println(String.format("SOLD ON Profit\n%s,", gson.toJson(position)));
        }
        else if ((difference < -cost * lossPercentage)
                || ((callIV - position.getBuy().call().getIV()) / position.getBuy().call().getIV()) > accetableIvIncrease
                || ((putIV - position.getBuy().put().getIV()) / position.getBuy().put().getIV()) > accetableIvIncrease) {
            /**
             * Book loss and exit.
             */
            LongStraddle.Straddle sell = new LongStraddle
                    .Straddle(
                    Trade.builder()
                            .timestamp(callTick.getLastTradedTime())
                            .tradeType(Trade.TradeType.SELL)
                            .symbol(position.getBuy().call().getSymbol())
                            .quantity(position.getBuy().call().getQuantity())
                            .premium(callTick.getLastTradedPrice())
                            .volumeTraded(callTick.getVolumeTradedToday())
                            .openInterestHigh(callTick.getOpenInterestDayHigh())
                            .spotPrice(spotPrice)
                            .IV(callIV)
                            .build(),
                    Trade.builder()
                            .timestamp(putTick.getLastTradedTime())
                            .tradeType(Trade.TradeType.SELL)
                            .symbol(position.getBuy().put().getSymbol())
                            .quantity(position.getBuy().put().getQuantity())
                            .premium(putTick.getLastTradedPrice())
                            .volumeTraded(putTick.getVolumeTradedToday())
                            .openInterestHigh(putTick.getOpenInterestDayHigh())
                            .spotPrice(spotPrice)
                            .IV(callIV)
                            .build());
            position.setSell(sell);
            position.setMaxLoss(-difference);
            position.setCurrentProfit(difference);
            currentPosition -= currentValue;
            /**
             * Here we make actual call to broker..
             */
            book.remove(strike);
            writer.println(String.format("SOLD ON Loss\n%s,",gson.toJson(position)));
        }
        else {
            position.setCurrentProfit(difference);
            if (difference < 0) {
                if (difference < -position.getMaxLoss()) {
                    position.setMaxLoss(-difference);
                    writer.println(String.format("UPDATING loss\n%s,",gson.toJson(position)));
                }
            }
            else {
                position.setProfitOpportunityCount(position.getProfitOpportunityCount() + 1);
                if (difference > position.getMaxProfit()) {
                    position.setMaxProfit(difference);
                    writer.println(String.format("UPDATING profit\n%s,",gson.toJson(position)));
                }
            }
        }
        writer.flush();
    }

    private String getCuirrentState() {
        int i = 0;
        StringBuffer state = new StringBuffer();
        state.append("=========================================\n");
        for (LongStraddle ls :book.values()) {

            state.append(String.format("%d. Budget = %.2f, currentPosition = %.25, Book = Buy Call = %.2f, Put = %.2f, Sell Call = %.2f, Put = %.2f\n",
                    i,
                    budget,
                    currentPosition,
                    ls.getBuy().call().getPremium(),
                    ls.getBuy().put().getPremium(),
                    ls.getSell() != null ? ls.getSell().call().getPremium() : 0.0,
                    ls.getSell() != null ? ls.getSell().put().getPremium() : 0.0));
            ++i;
        }
        return state.toString();
    }
    @Override
    public String toString() {
        return "TradeBook{" +
                "book=" + book +
                ", budget=" + budget +
                ", currentPosition=" + currentPosition +
                '}';
    }
}
