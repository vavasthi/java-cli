package org.avasthi.java.cli;

import com.zerodhatech.models.Tick;
import org.avasthi.java.cli.pojos.LongStraddle;
import org.avasthi.java.cli.pojos.SimulatedLongStraddle;
import org.avasthi.java.cli.pojos.SimulatedTrade;
import org.avasthi.java.cli.pojos.Trade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeBook {
    private final Map<Float, LongStraddle> book = new HashMap<>();
    private double budget;
    private double currentPosition;
    public TradeBook(double budget) {
        this.budget = budget;
        this.currentPosition = 0;
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
        }
        else {
            throw new RuntimeException("Insufficient funds");
        }
    }

    public void squareIfProfitableOrStopLoss(Float strike, double spotPrice, Tick callTick, Tick putTick, double callIV, double putIV, double profitPercentage, double lossPercentage) {
        LongStraddle position = book.get(strike);
        double currentValue = (callTick.getAverageTradePrice()*position.getBuy().call().getQuantity() + putTick.getLastTradedPrice() * position.getBuy().put().getQuantity());
        double cost = position.getCost();
        double difference = currentValue - cost;
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
            currentPosition -= currentValue;
            /**
             * Here we make actual call to broker..
             */
            book.remove(strike);
            System.out.println("SOLD ON Profit" + position.toString());
        }
        else if (difference > -cost * profitPercentage ) {
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
            currentPosition -= currentValue;
            /**
             * Here we make actual call to broker..
             */
            book.remove(strike);
            System.out.println("SOLD ON Loss" + position.toString());
        }
    }
}
