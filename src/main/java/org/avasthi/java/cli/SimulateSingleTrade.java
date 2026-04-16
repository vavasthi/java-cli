package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.opencsv.exceptions.CsvException;
import org.avasthi.java.cli.pojos.DailyVIX;
import org.avasthi.java.cli.pojos.TradeTick;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;

public class SimulateSingleTrade extends Base {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    SimulateSingleTrade lqr = new SimulateSingleTrade();
    lqr.simulateTrades(65, "NIFTY", .02);
  }

  private void simulateTrades(final long quantity,
                              final String asset,
                              final double expectedProfitPercentage) throws InterruptedException, ParseException {

    String header = "buyTimestamp,sellTimestamp,buySpot,buyCallPrice, buyPutPrice,buyVIX,buyCallIV,buyPutIV,sellSpot,sellCallPrice,sellPutPrice,sellVix,sellCallIV,sellPutIV";
    try (final PrintWriter pw = new PrintWriter("single-trade.csv");) {
      pw.println(header);
      popuateZerodhaInstrumentCollection();
      populateIndiaVix();
      Calendar buyTime = Calendar.getInstance();
      buyTime.set(2026, Calendar.MARCH, 27, 9, 15, 0);
      buyTime.set(Calendar.MILLISECOND, 0);
      Calendar e = Calendar.getInstance();
      e.set(2026, Calendar.APRIL, 7, 15, 30, 0);
      e.set(Calendar.MILLISECOND, 0);
      Date expiry = e.getTime();
      System.out.println(expiry);
      double strike = 23050;
      simulateSingleTrade(pw, quantity, asset, expectedProfitPercentage, expiry, buyTime, strike);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void simulateSingleTrade(PrintWriter pw,
                                   final long quantity,
                                   final String asset,
                                   final double expectedProfitPercentage,
                                   Date expiry,
                                   Calendar buyDate,
                                   double strike) {

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Calendar endOfDay = Calendar.getInstance();
    endOfDay.set(Calendar.MILLISECOND, 0);
    endOfDay.set(buyDate.get(Calendar.YEAR), buyDate.get(Calendar.MONTH), buyDate.get(Calendar.DAY_OF_MONTH), 15, 30, 0);

    Calendar current = Calendar.getInstance();
    current.setTime(buyDate.getTime());
    current.add(Calendar.SECOND, 1);
    Bson tradeTickQuery = Filters.and(
            Filters.eq("name", asset),
            Filters.eq("symbol", asset),
            Filters.eq("exchangeTimestamp", current.getTime())
    );
    MongoCollection<ZerodhaInstrument> zerodhaInstrumentMongoCollection = getZerodhaInstrumentsCollection();
    Bson instrumentFilter = Filters.and(
            Filters.eq("name", asset),
            Filters.eq("expiry", expiry),
            Filters.eq("strike", strike),
            Filters.eq("es", "NSE_OPTIONS")
    );
    ZerodhaInstrument callInstrument = null;
    ZerodhaInstrument putInstrument = null;
    MongoCursor<ZerodhaInstrument> cursor = zerodhaInstrumentMongoCollection.find(instrumentFilter).cursor();
    while (cursor.hasNext()) {
      ZerodhaInstrument zi = cursor.next();
      if (zi.instrumentType().equals("PE")) {
        putInstrument = zi;
      } else {
        callInstrument = zi;
      }
    }

    final TradeTick buySpotTradeTick  = getTradeTickCollection().find(tradeTickQuery).first();
      TradeTick buyCallTick = getTradeTickCollection().find(Filters.and(
              Filters.eq("symbol", callInstrument.symbol()),
              Filters.eq("exchangeTimestamp", buyDate.getTime()))).first();
      TradeTick buyPutTIck = getTradeTickCollection().find(Filters.and(
              Filters.eq("symbol", putInstrument.symbol()),
              Filters.eq("exchangeTimestamp", buyDate.getTime())
      )).first();
      double buyTimeToExpirationInYears = ImpliedVolatility.getTimeToExpiryInYears(buySpotTradeTick.exchangeTimestamp(), callInstrument.expiry());
      double buyCallIV = ImpliedVolatility.calculateIV(buyCallTick.lastPrice(), buySpotTradeTick.lastPrice(), strike, buyTimeToExpirationInYears, riskFreeRate, true);
      double buyPutIV = ImpliedVolatility.calculateIV(buyPutTIck.lastPrice(), buySpotTradeTick.lastPrice(), strike, buyTimeToExpirationInYears, riskFreeRate, false);
      float buyVix = getVix(buyDate.getTime());


      while (current.before(endOfDay)) {

        tradeTickQuery = Filters.and(
                Filters.eq("name", asset),
                Filters.eq("symbol", asset),
                Filters.eq("exchangeTimestamp", current.getTime())
        );
        final TradeTick currentSpotTradeTick = getTradeTickCollection().find(tradeTickQuery).first();
        TradeTick currentCallTick = getTradeTickCollection().find(Filters.and(
                Filters.eq("symbol", callInstrument.symbol()),
                Filters.eq("exchangeTimestamp", current.getTime()))).first();
        TradeTick currentPutTIck = getTradeTickCollection().find(Filters.and(
                Filters.eq("symbol", putInstrument.symbol()),
                Filters.eq("exchangeTimestamp", current.getTime())
        )).first();
        if (currentSpotTradeTick != null && currentCallTick != null && currentPutTIck != null) {

          double currentTimeToExpirationInYears = ImpliedVolatility.getTimeToExpiryInYears(currentCallTick.exchangeTimestamp(), callInstrument.expiry());
          double currentCallIV = ImpliedVolatility.calculateIV(currentCallTick.lastPrice(), currentSpotTradeTick.lastPrice(), strike, currentTimeToExpirationInYears, riskFreeRate, true);
          double currentPutIV = ImpliedVolatility.calculateIV(currentPutTIck.lastPrice(), currentSpotTradeTick.lastPrice(), strike, currentTimeToExpirationInYears, riskFreeRate, false);
          float currentVix = getVix(buyDate.getTime());
          pw.println(String.format("%s,%s,%.2f,%.2f,%.2f,%.4f,%.4f,%.4f,%.2f,%.2f,%.2f,%.4f,%.4f,%.4f",
                  sdf.format(buyDate.getTime()),
                  sdf.format(current.getTime()),
                  buySpotTradeTick.lastPrice(),
                  buyCallTick.lastPrice() * quantity,
                  buyPutTIck.lastPrice() * quantity,
                  buyVix,
                  buyCallIV,
                  buyPutIV,
                  currentSpotTradeTick.lastPrice(),
                  currentCallTick.lastPrice() * quantity,
                  currentPutTIck.lastPrice() * quantity,
                  currentVix,
                  currentCallIV,
                  currentPutIV));
        }
        current.add(Calendar.SECOND, 1);
      }
  }

  private float getVix(Date timestamp) {

    try {

      TradeTick tt = getTradeTickCollection().find(Filters.and(Filters.eq("symbol", "INDIA VIX"), Filters.eq("exchangeTimestamp", timestamp))).first();
      return tt.lastPrice();
    } catch (Exception e) {
      Calendar fromC = Calendar.getInstance();
      fromC.setTime(timestamp);
      fromC.add(Calendar.SECOND, -1);
      Date from = fromC.getTime();
      fromC.add(Calendar.SECOND, 2);
      Date to = fromC.getTime();
      try {

        TradeTick tt = getTradeTickCollection().find(Filters.and(Filters.eq("symbol", "INDIA VIX"), Filters.gte("exchangeTimestamp", from), Filters.lte("exchangeTimestamp", to))).sort(ascending("exchangeTimestamp")).first();
        return tt.lastPrice();
      } catch (Exception e1) {

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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}