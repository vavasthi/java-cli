package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import com.opencsv.exceptions.CsvException;
import com.zerodhatech.models.Trade;
import org.avasthi.java.cli.pojos.*;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

record OptionPair(ZerodhaInstrument call, ZerodhaInstrument put) {

}
public class StraddleSimulator extends Base {

  ExecutorService executorService = Executors.newFixedThreadPool(15);

  Map<Long, OptionPair> optionMap = new HashMap<>();
  public StraddleSimulator() throws IOException, CsvException {

  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    StraddleSimulator lqr = new StraddleSimulator();
    lqr.simulateTrades(1, "NIFTY", .02);
  }
  private void simulateTrades(final long quantity,
                              final String asset,
                              final double expectedProfitPercentage) throws InterruptedException {

    Calendar from = Calendar.getInstance();
    from.set(2026, Calendar.FEBRUARY, 26, 0, 0 ,0);
    Calendar to = Calendar.getInstance();
    to.set(2026, Calendar.MARCH, 14, 0, 0 ,0);
    while (from.before(to)) {

      simulateTrades(quantity, asset, expectedProfitPercentage, from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DAY_OF_MONTH));
      from.add(Calendar.DAY_OF_MONTH, 1);
    }
    executorService.shutdown();
  }

  private void simulateTrades(final long quantity,
                              final String asset,
                              final double expectedProfitPercentage,
                              final int yy,
                              final int mm,
                              final int dd) throws InterruptedException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(yy, mm, dd, 9, 15, 0);
    Date startDay = calendar.getTime();
    calendar.set(yy, mm, dd, 15, 30, 0);
    Date endDay = calendar.getTime();
    Date expiry = nextExpiry(endDay);
    System.out.println(expiry);
    MongoCollection<ZerodhaInstrument> zerodhaInstrumentMongoCollection = getZerodhaInstrumentsCollection();
    Bson instrumentFilter = Filters.and(
            Filters.eq("name", asset),
            Filters.eq("expiry", expiry),
            Filters.eq("es", "NSE_OPTIONS")
    );
    zerodhaInstrumentMongoCollection.find(instrumentFilter).forEach(zi -> {
      OptionPair op = optionMap.get(zi.strike());
      if (op == null) {
        optionMap.put(zi.strike(), new OptionPair(zi.instrumentType().equals("CE") ? zi : null, zi.instrumentType().equals("PE") ? zi : null));
      } else {
        optionMap.put(zi.strike(), new OptionPair(zi.instrumentType().equals("CE") ? zi : op.call(), zi.instrumentType().equals("PE") ? zi : op.put()));
      }
    });
    MongoCollection<TradeTick> tradeTickMongoCollection = getTradeTickCollection();
    MongoCollection<SimulatedLongStraddle> simulatedLongStraddleCollection = getMongoClient().getDatabase(database).getCollection(simulatedTradeName, SimulatedLongStraddle.class);
    simulatedLongStraddleCollection.deleteMany(Filters.and(
            Filters.gte("timestamp", startDay),
            Filters.lte("timestamp", endDay)
    ));
    final Calendar timeCounter = Calendar.getInstance();
    timeCounter.setTime(startDay);

    while (timeCounter.getTime().before(endDay)) {
      System.out.println("Starting for timestamp " + timeCounter.getTime());

      Bson tradeTickQuery = Filters.and(
              Filters.eq("name", asset),
              Filters.eq("symbol", asset),
              Filters.eq("exchangeTimestamp", timeCounter.getTime())
      );
      MongoCursor<TradeTick> tradeTickMongoCursor = tradeTickMongoCollection.find(tradeTickQuery).cursor();
      if (tradeTickMongoCursor.hasNext()) {
        final TradeTick spotTradeTick = tradeTickMongoCursor.next();
        final List<Callable<Boolean>> callableList = new ArrayList<>();
        optionMap.entrySet().forEach(op -> {
          callableList.add(() -> processOneTimestamp(timeCounter, op.getValue(), spotTradeTick, expectedProfitPercentage, asset, quantity));
        });
        executorService.invokeAll(callableList);
        callableList.clear();
      }
      else {
        System.out.println("No trade tick found for spot value for " + timeCounter.getTime());
      }
      timeCounter.add(Calendar.SECOND, 1);
    }
  }

    /*
    System.out.println(tradeTickQuery.toBsonDocument().toJson().toString());
    Date currentTimestamp = null;
    double spot = 0.0;
    while(tradeTickMongoCursor.hasNext()) {
      TradeTick tradeTick = tradeTickMongoCursor.next();
      System.out.println(tradeTick);
      ZerodhaInstrument zi = zerodhaInstrumentMongoCollection.find(Filters.eq("symbol", tradeTick.symbol())).first();
      if (zi.instrumentType().equals("CE") || zi.instrumentType().equals("PE")) {

        if (currentTimestamp == null || !tradeTick.exchangeTimestamp().equals(currentTimestamp)) {
          currentTimestamp = tradeTick.exchangeTimestamp();
          spot = getSpotValue(tradeTickMongoCollection, tradeTick.exchangeTimestamp(), "NIFTY", "NIFTY");
        }
        System.out.println(zi.strike() + "  " + relevantStrikes + " " + spot);


//        tick.getLastTradedPrice(), spotPrice, ZerodhaInstrument.strike(), timeToExpirationInYear, riskFreeRate, ZerodhaInstrument.symbol().endsWith("CE")
      }
      }*/


  private Boolean processOneTimestamp(Calendar timeCounter,
                                      OptionPair op,
                                      TradeTick spotTradeTick,
                                      double expectedProfitPercentage,
                                      String asset,
                                      long quantity) {

    MongoCollection<SimulatedLongStraddle> simulatedLongStraddleCollection = getMongoClient().getDatabase(database).getCollection(simulatedTradeName, SimulatedLongStraddle.class);
    Bson callOptionQuery = Filters.and(
            Filters.eq("symbol", op.call().symbol()),
            Filters.eq("exchangeTimestamp", timeCounter.getTime())
    );
    TradeTick callTradeTick = null;
    MongoCursor<TradeTick> callCursor = getTradeTickCollection().find(callOptionQuery).cursor();
    if (callCursor.hasNext()) {

      callTradeTick = callCursor.next();
    }
    Bson putOptionQuery = Filters.and(
            Filters.eq("symbol", op.put().symbol()),
            Filters.eq("exchangeTimestamp", timeCounter.getTime())
    );
    TradeTick putTradeTick = null;
    MongoCursor<TradeTick> putCursor = getTradeTickCollection().find(putOptionQuery).cursor();
    if (putCursor.hasNext()) {

      putTradeTick = putCursor.next();
    }

    if (callTradeTick != null && putTradeTick != null) {
      /**
       * Calsulate IVs for these ticks
       */
      double timeToExpirationInYears = getTimeToExpiryInYears(spotTradeTick.exchangeTimestamp(), op.call().expiry());
      double callIV = ImpliedVolatility.calculateIV(callTradeTick.lastPrice(), spotTradeTick.lastPrice(), op.call().strike(), timeToExpirationInYears, riskFreeRate, true);
      double putIV = ImpliedVolatility.calculateIV(putTradeTick.lastPrice(), spotTradeTick.lastPrice(), op.put().strike(), timeToExpirationInYears, riskFreeRate, false);

      //        tick.getLastTradedPrice(), spotPrice, ZerodhaInstrument.strike(), timeToExpirationInYear, riskFreeRate, ZerodhaInstrument.symbol().endsWith("CE")

      /**
       * TradeTick is available for both the call and put option at this strike rate. In this simulation we always buy
       * call and put options at the same strike rate. Now we check if we already have a trade at this strike pair.
       */
      MongoCursor<SimulatedLongStraddle> cursor = simulatedLongStraddleCollection.find(Filters.eq("strike", op.call().strike())).cursor();
      while (cursor.hasNext()) {
        SimulatedLongStraddle trade = cursor.next();
        /**
         * A trade at this strike already exists. Let's see if we can make profit on this.
         */
        updateSellRecord(simulatedLongStraddleCollection, trade, callTradeTick, putTradeTick, op, callIV, putIV, expectedProfitPercentage);
      }
      /**
       * Add a new buy trade at this price.
       */
      Set<Long> relevantStrikes = findNearestStrike(spotTradeTick.lastPrice(), 50);
      if (relevantStrikes.contains(op.call().strike()) && relevantStrikes.contains(op.put().strike())) {

        SimulatedLongStraddle trade = SimulatedLongStraddle.builder()
                .tradeId(UUID.randomUUID())
                .asset(asset)
                .buy(new SimulatedLongStraddle.Straddle(
                        SimulatedTrade.builder()
                                .timestamp(callTradeTick.exchangeTimestamp())
                                .tradeType(SimulatedTrade.TradeType.BUY)
                                .symbol(op.call().symbol())
                                .quantity(quantity)
                                .premium(callTradeTick.lastPrice())
                                .IV(callIV)
                                .build(),
                        SimulatedTrade.builder()
                                .timestamp(putTradeTick.exchangeTimestamp())
                                .tradeType(SimulatedTrade.TradeType.BUY)
                                .symbol(op.put().symbol())
                                .quantity(quantity)
                                .premium(putTradeTick.lastPrice())
                                .IV(putIV)
                                .build()
                ))
                .profitOpportunityCount(0)
                .timestamp(spotTradeTick.exchangeTimestamp())
                .strike(op.call().strike())
                .build();
        simulatedLongStraddleCollection.insertOne(trade);
      }
      return true;
    }
    return null;
  }
  private Date nextExpiry(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
      calendar.add(Calendar.DAY_OF_MONTH, 1);
    }
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }
  /**
   * Compute the profit based on the current tick and update the record if the profit is more than previous instance.
   * If it is less than max profit. Just update the count
   * @param simulatedLongStraddleCollection
   * @param trade
   * @param callTradeTick
   * @param putTradeTick
   * @param op
   * @param callIV
   * @param putIV
   */
  private void updateSellRecord(MongoCollection<SimulatedLongStraddle> simulatedLongStraddleCollection,
                                SimulatedLongStraddle trade,
                                TradeTick callTradeTick,
                                TradeTick putTradeTick,
                                OptionPair op,
                                double callIV,
                                double putIV,
                                double expectedProfitPercentage) {

    final long callQuantity = trade.getBuy().call().getQuantity();
    final long putQuantity = trade.getBuy().put().getQuantity();

    double currentPrice = callTradeTick.lastPrice() * callQuantity + putTradeTick.lastPrice() * putQuantity;
    double cost = callQuantity * trade.getBuy().call().getPremium() + putQuantity * trade.getBuy().put().getPremium();
    double profit = currentPrice - cost;
    double expectedProfit = cost * expectedProfitPercentage;
    if (profit > expectedProfit) {
      /** Potential profit here. Check if it more than last time. We are checking for more than 10%
       *
       */
      if (trade.getOtherSellOpportunities() == null) {
        trade.setOtherSellOpportunities(new ArrayList<>());
      }
      SimulatedLongStraddle.Straddle sell = new SimulatedLongStraddle
              .Straddle(
              SimulatedTrade.builder()
                      .timestamp(callTradeTick.exchangeTimestamp())
                      .tradeType(SimulatedTrade.TradeType.SELL)
                      .symbol(op.call().symbol())
                      .quantity(trade.getBuy().call().getQuantity())
                      .premium(callTradeTick.lastPrice())
                      .IV(callIV)
                      .build(),
              SimulatedTrade.builder()
                      .timestamp(putTradeTick.exchangeTimestamp())
                      .tradeType(SimulatedTrade.TradeType.SELL)
                      .symbol(op.put().symbol())
                      .quantity(trade.getBuy().put().getQuantity())
                      .premium(putTradeTick.lastPrice())
                      .IV(putIV)
                      .build());
      if (profit > trade.getMaxProfit()) {
        /**
         * We have more profit thaan what we see last time. Let's replace the old profitl
         */
        if (trade.getSell() != null) {

          trade.getOtherSellOpportunities().add(trade.getSell());
        }
        trade.setSell(sell);
        trade.setMaxProfit(profit);

      }
      else {
        trade.getOtherSellOpportunities().add(sell);
      }
      trade.setProfitOpportunityCount(trade.getProfitOpportunityCount() + 1);
      simulatedLongStraddleCollection.replaceOne(Filters.eq("tradeId", trade.getTradeId()), trade);
    }
  }

  private Set<Long> findNearestStrike(double strike, int expiryBoundaries) {
    long lowerExpiry = (long) Math.floor(strike/expiryBoundaries) * 50;
    long upperExpiry = lowerExpiry + expiryBoundaries;
    return Arrays.asList(lowerExpiry, upperExpiry).stream().collect(Collectors.toSet());
  }
  private double getSpotValue(MongoCollection<TradeTick> tradeTickMongoCollection, Date timestamp, String name, String symbol) {
    Bson query = Filters.and(
            Filters.eq("name", name),
            Filters.eq("symbol", symbol),
            Filters.eq("exchangeTimestamp", timestamp)
    );
    List<TradeTick> tradeTickList = new ArrayList<>();
    double sum = 0;
    double count = 0;
    MongoCursor<TradeTick> cursor = tradeTickMongoCollection.find(query).cursor();
    while(cursor.hasNext()) {

      sum += cursor.next().lastPrice();
      ++count;
    }
    return sum/count;
  }
}