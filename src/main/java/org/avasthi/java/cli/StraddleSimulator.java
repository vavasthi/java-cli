package org.avasthi.java.cli;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.*;
import com.opencsv.exceptions.CsvException;
import org.avasthi.java.cli.pojos.*;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;

record TradeUpdates(List<SimulatedLongStraddle> deleteList, List<SimulatedLongStraddle> insertList, List<SimulatedLongStraddle> replaceList) {

}
public class StraddleSimulator extends Base {

  ExecutorService executorService = Executors.newFixedThreadPool(20);

  Map<Float, OptionPair> optionMap = new HashMap<>();
  public StraddleSimulator() throws IOException, CsvException {

  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    StraddleSimulator lqr = new StraddleSimulator();
    lqr.simulateTrades(1, "NIFTY", .02);
  }
  private void simulateTrades(final long quantity,
                              final String asset,
                              final double expectedProfitPercentage) throws InterruptedException, ParseException {

    popuateZerodhaInstrumentCollection();
    populateIndiaVix();
    Calendar from = Calendar.getInstance();
    from.set(2026, Calendar.MARCH, 18, 0, 0 ,0);
    Calendar to = Calendar.getInstance();
    to.set(2026, Calendar.MARCH, 19, 0, 0 ,0);
    Calendar e = Calendar.getInstance();
    e.set(2026,Calendar.MARCH,24,15,30,00);
    e.set(Calendar.MILLISECOND, 0);
    Date expiry = e.getTime();
    System.out.println(expiry);
    while (from.before(to)) {

      simulateTrades(quantity, asset, expectedProfitPercentage, expiry, from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DAY_OF_MONTH));
      from.add(Calendar.DAY_OF_MONTH, 1);
    }
    executorService.shutdown();
  }

  private void simulateTrades(final long quantity,
                              final String asset,
                              final double expectedProfitPercentage,
                              Date expiry,
                              final int yy,
                              final int mm,
                              final int dd) throws InterruptedException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(yy, mm, dd, 9, 15, 0);
    Date startDay = calendar.getTime();
    calendar.set(yy, mm, dd, 15, 30, 0);
    Date endDay = calendar.getTime();
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

    final List<WriteModel<SimulatedLongStraddle>> allWrites = new ArrayList<>();
    final Set<UUID> movedSet = new HashSet<>();
    final Map<Float, List<SimulatedLongStraddle>> insertedRecordMap = new HashMap<>();
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
        optionMap.entrySet().forEach(op -> {
          TradeUpdates tu = processOneTimestamp(insertedRecordMap, timeCounter, op.getValue(), spotTradeTick, expectedProfitPercentage, asset, quantity);
          tu.deleteList().forEach(d -> {
//            allWrites.add(new DeleteOneModel<SimulatedLongStraddle>(Filters.eq("tradeId", d.getTradeId())));
            movedSet.add(d.getTradeId());
          });
          tu.insertList().forEach( i -> {
            allWrites.add(new InsertOneModel<SimulatedLongStraddle>(i));
          });
          tu.replaceList().forEach(r -> {
 //           allWrites.add(new ReplaceOneModel<SimulatedLongStraddle>(Filters.eq("tradeId", r.getTradeId()), r));
          });
        });
      }
      else {
        System.out.println("No trade tick found for spot value for " + timeCounter.getTime());
      }
      timeCounter.add(Calendar.SECOND, 1);
    }
    if (!allWrites.isEmpty()) {

      BulkWriteResult bwr = simulatedLongStraddleCollection.bulkWrite(allWrites, new BulkWriteOptions().ordered(true));
//      BulkWriteResult bwr = simulatedLongStraddleCollection.bulkWrite(allWrites, new BulkWriteOptions().ordered(true));
      System.out.println(String.format("INSERTING Database update, Deleted %d Inserted %d Replaced %d", bwr.getDeletedCount(), bwr.getInsertedCount(), bwr.getModifiedCount()));
      allWrites.clear();
    }
    if (!movedSet.isEmpty()) {

      Bson recordsMarkedToBeDeleted = Filters.in("tradeId", movedSet);
      simulatedLongStraddleCollection.aggregate(Arrays.asList(
              Aggregates.match(recordsMarkedToBeDeleted),
              Aggregates.merge("deletedExpensiveSimulations", new MergeOptions().whenMatched(MergeOptions.WhenMatched.MERGE).whenNotMatched(MergeOptions.WhenNotMatched.INSERT))
      ));
      simulatedLongStraddleCollection.deleteMany(recordsMarkedToBeDeleted);
      movedSet.clear();
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


  private float getVix(Date timestamp) {

    try {

      TradeTick tt = getTradeTickCollection().find(Filters.and(Filters.eq("symbol", "INDIA VIX"), Filters.eq("exchangeTimestamp", timestamp))).first();
      return tt.lastPrice();
    }
    catch (Exception e) {
      Calendar fromC = Calendar.getInstance();
      fromC.setTime(timestamp);
      fromC.add(Calendar.SECOND, -1);
      Date from = fromC.getTime();
      fromC.add(Calendar.SECOND, 2);
      Date to = fromC.getTime();
      try {

        TradeTick tt = getTradeTickCollection().find(Filters.and(Filters.eq("symbol", "INDIA VIX"), Filters.gte("exchangeTimestamp", from), Filters.lte("exchangeTimestamp", to))).sort(ascending("exchangeTimestamp")).first();
        return tt.lastPrice();
      }
      catch (Exception e1) {

      }

    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(timestamp);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date to = calendar.getTime();
    calendar.add(Calendar.DAY_OF_MONTH, -2);
    Date from = calendar.getTime();
    MongoCollection<DailyVIX> dailyVIXMongoCollection = getDailyVixCollection();
    Bson vixFilter = Filters.and(Filters.gte("date", from), Filters.lte("date", to));
    try {

      DailyVIX dailyVIX = dailyVIXMongoCollection.find(vixFilter).sort(descending("date")).first();
      return dailyVIX.open();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  private TradeUpdates processOneTimestamp(Map<Float, List<SimulatedLongStraddle>> insertedRecordMap,
                                           Calendar timeCounter,
                                           OptionPair op,
                                           TradeTick spotTradeTick,
                                           double expectedProfitPercentage,
                                           String asset,
                                           long quantity) {

    final float vix = getVix(spotTradeTick.exchangeTimestamp());
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

    List<SimulatedLongStraddle> deleteList = new ArrayList<>();
    List<SimulatedLongStraddle> insertList = new ArrayList<>();
    List<SimulatedLongStraddle> replaceList =  new ArrayList<>();
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
       * We only look for trades from today.
       */

      List<SimulatedLongStraddle> insertedRecordList = insertedRecordMap.get(op.call().strike());
      boolean needToInsert = true;
      if (insertedRecordList != null && !insertedRecordList.isEmpty()) {
        for (SimulatedLongStraddle trade : insertedRecordList) {
          /**
           * A trade at this strike already exists. Let's see if we can make profit on this. If it can be profitable, we \
           * update sell details, otherwise we check if this is a cheaper option than previosly existing one and we add it
           * as a new buy.
           */
          needToInsert = updateSellRecord(trade, callTradeTick, putTradeTick, op, callIV, putIV, expectedProfitPercentage, vix, deleteList, replaceList);
        }
      }
      else {
        insertedRecordList = new ArrayList<>();
        insertedRecordMap.put(op.call().strike(), insertedRecordList);
        needToInsert = true;
      }
      /**
       * Add a new buy trade at this price if there is no trade existing at
       *
       * this strike price. If the trade was found at
       * this price then found will be set to true.
       */
      if (needToInsert) {

        Set<Float> relevantStrikes = findNearestStrike(spotTradeTick.lastPrice(), 50);
        if (relevantStrikes.contains(op.call().strike()) && relevantStrikes.contains(op.put().strike())) {

          SimulatedLongStraddle newTrade = SimulatedLongStraddle.builder()
                  .tradeId(UUID.randomUUID())
                  .asset(asset)
                  .buy(new SimulatedLongStraddle.Straddle(
                          SimulatedTrade.builder()
                                  .timestamp(callTradeTick.exchangeTimestamp())
                                  .tradeType(SimulatedTrade.TradeType.BUY)
                                  .symbol(op.call().symbol())
                                  .quantity(quantity)
                                  .premium(callTradeTick.lastPrice())
                                  .volumeTraded(callTradeTick.volumeTraded())
                                  .openInterest(callTradeTick.openInterest())
                                  .spotPrice(spotTradeTick.lastPrice())
                                  .IV(callIV)
                                  .build(),
                          SimulatedTrade.builder()
                                  .timestamp(putTradeTick.exchangeTimestamp())
                                  .tradeType(SimulatedTrade.TradeType.BUY)
                                  .symbol(op.put().symbol())
                                  .quantity(quantity)
                                  .premium(putTradeTick.lastPrice())
                                  .volumeTraded(putTradeTick.volumeTraded())
                                  .openInterest(putTradeTick.openInterest())
                                  .spotPrice(spotTradeTick.lastPrice())
                                  .IV(putIV)
                                  .build()
                  ))
                  .profitOpportunityCount(0)
                  .maxLoss(0)
                  .maxProfit(0)
                  .timestamp(callTradeTick.exchangeTimestamp())
                  .strike(op.call().strike())
                  .spotPrice(spotTradeTick.lastPrice())
                  .vix(vix)
                  .build();
          insertList.add(newTrade);
          insertedRecordList.add(newTrade);
        }
      }
    }
    return new TradeUpdates(deleteList, insertList, replaceList);
  }
  private Date nextExpiry(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
      calendar.add(Calendar.DAY_OF_MONTH, 1);
    }
    calendar.set(Calendar.HOUR_OF_DAY, 15);
    calendar.set(Calendar.MINUTE, 30);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }
  /**
   * Compute the profit based on the current tick and update the record if the profit is more than previous instance.
   * If it is less than max profit. Just update the count
   * @param trade
   * @param callTradeTick
   * @param putTradeTick
   * @param op
   * @param callIV
   * @param putIV
   */
  private boolean updateSellRecord(SimulatedLongStraddle trade,
                                   TradeTick callTradeTick,
                                   TradeTick putTradeTick,
                                   OptionPair op,
                                   double callIV,
                                   double putIV,
                                   double expectedProfitPercentage,
                                   float vix,
                                   List<SimulatedLongStraddle> deleteList,
                                   List<SimulatedLongStraddle> replaceList) {

    final long callQuantity = trade.getBuy().call().getQuantity();
    final long putQuantity = trade.getBuy().put().getQuantity();

    double currentPrice = callTradeTick.lastPrice() * callQuantity + putTradeTick.lastPrice() * putQuantity;
    double cost = callQuantity * trade.getBuy().call().getPremium() + putQuantity * trade.getBuy().put().getPremium();
    double profit = currentPrice - cost;
    double expectedProfit = cost * expectedProfitPercentage;
    boolean needToInsert = false;
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
                      .volumeTraded(callTradeTick.volumeTraded())
                      .openInterest(callTradeTick.openInterest())
                      .spotPrice(trade.getSpotPrice())
                      .IV(callIV)
                      .build(),
              SimulatedTrade.builder()
                      .timestamp(putTradeTick.exchangeTimestamp())
                      .tradeType(SimulatedTrade.TradeType.SELL)
                      .symbol(op.put().symbol())
                      .quantity(trade.getBuy().put().getQuantity())
                      .premium(putTradeTick.lastPrice())
                      .volumeTraded(putTradeTick.volumeTraded())
                      .openInterest(putTradeTick.openInterest())
                      .spotPrice(trade.getSpotPrice())
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
        trade.setVix(vix);

      }
      else {
        trade.getOtherSellOpportunities().add(sell);
        trade.setVix(vix);
      }
      trade.setProfitOpportunityCount(trade.getProfitOpportunityCount() + 1);
      replaceList.add(trade);
    }
    else if (profit < 0 && profit < trade.getMaxLoss()) {
      trade.setMaxLoss(profit);
      replaceList.add(trade);
      needToInsert = true;
    }
    else if (callQuantity * callTradeTick.lastPrice() + putQuantity * putTradeTick.lastPrice() < cost) {
      /**
       * This option seems cheaper than a previously existing option. Let's add it as a new trade.
       */
      needToInsert = true;
      if (trade.getSell() == null) {
        /**
         * Older buy has no sells yet, we can safely remove this one.
         */
        deleteList.add(trade);
      }
    }
    return needToInsert;
  }

  private Set<Float> findNearestStrike(double strike, int expiryBoundaries) {
    float lowerExpiry = (long) Math.floor(strike/expiryBoundaries) * 50;
    float upperExpiry = lowerExpiry + expiryBoundaries;
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