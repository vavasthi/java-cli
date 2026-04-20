package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.avasthi.java.cli.pojos.*;
import org.bson.conversions.Bson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;

public class OptionPairSelling extends Base {

  private final String asset = "NIFTY";
  private final String niftyOptionsName = "NIFTY";
  private final String niftyOptionsEs = "NSE_OPTIONS";
  private final float niftyStrikeBoundary = 50;
  private final int spreadStrikes = 10;

  private final float profitPercentage;
  private final float stopLossPercentage;
  private final MongoCollection<TradeTick> tradeTickMongoCollection = getTradeTickCollection();
  Map<Float, OptionSellSpread> optionSpreadMap = new HashMap<>();
  List<OptionSellSpread> finishedOptionSpreads = new ArrayList<>();
  public OptionPairSelling(float profitPercentage, float stopLossPercentage) throws IOException, CsvException, ParseException {

    popuateZerodhaInstrumentCollection();
    populateIndiaVix();
    this.profitPercentage = profitPercentage;
    this.stopLossPercentage = stopLossPercentage;
  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    OptionPairSelling lqr = new OptionPairSelling(.1f, .1f);
    lqr.run();
  }

  private void run() throws ParseException, IOException {

    popuateZerodhaInstrumentCollection();
    populateIndiaVix();
    MongoCollection<TradeTick> tradeTickMongoCollection = getTradeTickCollection();
    MongoCursor<TradeTick> cursor = tradeTickMongoCollection.find(Filters.and(
            Filters.eq("name", niftyOptionsName),
            Filters.eq("symbol", niftyOptionsName)
    )).sort(Sorts.ascending("exchangeTimestamp")).cursor();
    while(cursor.hasNext()) {
      TradeTick spotTradeTick = cursor.next();
      run(tradeTickMongoCollection, spotTradeTick);
    }
    int count = 1;
    SimpleDateFormat ddmm = new SimpleDateFormat("ddMMM");
    try (Workbook workbook = new XSSFWorkbook()) {

      for (OptionSellSpread sellSpread : finishedOptionSpreads) {
        String sheetName = String.format("%s_%d_%d", ddmm.format(sellSpread.getPutSell().getZerodhaInstrument().expiry()),
                (int)(sellSpread.getCallSell().getZerodhaInstrument().strike()), count++);
        writeSellSpreadSheet(workbook, sheetName, sellSpread);
      }
      for (Map.Entry<Float, OptionSellSpread> e : optionSpreadMap.entrySet()) {

        OptionSellSpread sellSpread = e.getValue();
        String sheetName = String.format("%s_%d_%d", ddmm.format(sellSpread.getPutSell().getZerodhaInstrument().expiry()),
                (int)(sellSpread.getPutSell().getZerodhaInstrument().strike()), count++);
        writeSellSpreadSheet(workbook, sheetName, sellSpread);
      }
      try (FileOutputStream out = new FileOutputStream("sell-spread.xlsx")) {
        workbook.write(out);
        out.flush();
        out.close();
      }
    }
    catch (Exception e) {}

  }
  private void run(MongoCollection<TradeTick> tradeTickCollection,
                   TradeTick spot) {
    OptionSellSpread sellSpread = sellSpread(spot.exchangeTimestamp(), spot.lastPrice(), niftyStrikeBoundary);
    if (sellSpread.isValid()) {

      TradeTick putSellTick = tradeTickCollection.find(Filters.and(
              Filters.eq("symbol", sellSpread.getPutSell().getZerodhaInstrument().symbol()),
              Filters.eq("exchangeTimestamp", spot.exchangeTimestamp())
      )).first();
      TradeTick callSellTick = tradeTickCollection.find(Filters.and(
              Filters.eq("symbol", sellSpread.getCallSell().getZerodhaInstrument().symbol()),
              Filters.eq("exchangeTimestamp", spot.exchangeTimestamp())
      )).first();
      if (putSellTick != null && callSellTick != null) {

        sellSpread.updateTicks(putSellTick, callSellTick, spot, profitPercentage, stopLossPercentage);
        if (sellSpread.getStatus() != OptionSellSpread.STATUS.RUNNING) {
          System.out.printf("Optiuon trade complete %.0f for symbol %s and %s with profit %.2f\n", sellSpread.getCallSell().getZerodhaInstrument().strike(),
                  sellSpread.getCallSell().getZerodhaInstrument().symbol(),
                  sellSpread.getPutSell().getZerodhaInstrument().symbol(),
                  sellSpread.getProfit());
          optionSpreadMap.remove(sellSpread.getPutSell().getZerodhaInstrument().strike());
          finishedOptionSpreads.add(sellSpread);
        }
      }
    }
  }


  private void writeSellSpreadSheet(Workbook workbook, String sheetName, OptionSellSpread sellSpread) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Sheet sheet = workbook.getSheet(sheetName);
    if (sheet == null) {
      sheet = workbook.createSheet(sheetName);
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Expiry");
      header.createCell(1).setCellValue("Timestamp");
      header.createCell(2).setCellValue("Spot Price");

      header.createCell(3).setCellValue("Put Sell  Strike");
      header.createCell(4).setCellValue("Put Sell  Price");
      header.createCell(5).setCellValue("Put Sell  Lot");
      header.createCell(6).setCellValue("Put IV");

      header.createCell(7).setCellValue("Call Sell Strike");
      header.createCell(8).setCellValue("Call Sell Price");
      header.createCell(9).setCellValue("Call Sell Lot");
      header.createCell(10).setCellValue("Call IV");

      header.createCell(11).setCellValue("Put Sell Square Price");
      header.createCell(12).setCellValue("Put IV");
      header.createCell(13).setCellValue("Call Sell Square Price");
      header.createCell(14).setCellValue("Call IV");

      header.createCell(15).setCellValue("Status");

    }
    Date expiry = sellSpread.getPutSell().getZerodhaInstrument().expiry();
    for (OptionSellSpread.TradeTicks tt : sellSpread.getOtherTicks()) {

      Row data = sheet.createRow(sheet.getLastRowNum() + 1);

      double buyPutIV = ImpliedVolatility.calculateIV(tt.putSell().lastPrice(),
              tt.spot().lastPrice(),
              sellSpread.getPutSell().getZerodhaInstrument().strike(),
              sellSpread.getPutSell().getInitialTick().exchangeTimestamp(),
              sellSpread.getPutSell().getZerodhaInstrument().expiry(),
              riskFreeRate,
              false);

      double buyCallIV = ImpliedVolatility.calculateIV(tt.callSell().lastPrice(),
              tt.spot().lastPrice(),
              sellSpread.getCallSell().getZerodhaInstrument().strike(),
              sellSpread.getCallSell().getInitialTick().exchangeTimestamp(),
              sellSpread.getCallSell().getZerodhaInstrument().expiry(),
              riskFreeRate,
              true);

      double sellPutIV = ImpliedVolatility.calculateIV(tt.putSell().lastPrice(),
              tt.spot().lastPrice(),
              sellSpread.getPutSell().getZerodhaInstrument().strike(),
              tt.putSell().exchangeTimestamp(),
              sellSpread.getPutSell().getZerodhaInstrument().expiry(),
              riskFreeRate,
              false);

      double sellCallIV = ImpliedVolatility.calculateIV(tt.callSell().lastPrice(),
              tt.spot().lastPrice(),
              sellSpread.getCallSell().getZerodhaInstrument().strike(),
              tt.callSell().exchangeTimestamp(),
              sellSpread.getCallSell().getZerodhaInstrument().expiry(),
              riskFreeRate,
              true);

      data.createCell(0).setCellValue(formatter.format(expiry));;
      data.createCell(1).setCellValue(formatter.format(tt.putSell().exchangeTimestamp()));
      data.createCell(2).setCellValue(tt.spot().lastPrice());

      data.createCell(3).setCellValue(sellSpread.getPutSell().getZerodhaInstrument().strike());;
      data.createCell(4).setCellValue(sellSpread.getPutSell().getInitialTick().lastPrice());;
      data.createCell(5).setCellValue(sellSpread.getPutSell().getZerodhaInstrument().lotSize());;
      data.createCell(6).setCellValue(buyPutIV);;

      data.createCell(7).setCellValue(sellSpread.getCallSell().getZerodhaInstrument().strike());;
      data.createCell(8).setCellValue(sellSpread.getCallSell().getInitialTick().lastPrice());;
      data.createCell(9).setCellValue(sellSpread.getCallSell().getZerodhaInstrument().lotSize());;
      data.createCell(10).setCellValue(buyCallIV);;

      data.createCell(11).setCellValue(tt.putSell().lastPrice());;
      data.createCell(12).setCellValue(sellPutIV);;

      data.createCell(13).setCellValue(tt.callSell().lastPrice());;
      data.createCell(14).setCellValue(sellCallIV);;

      data.createCell(15).setCellValue(tt.status().getStatus());;
    }
  }

  private OptionSellSpread sellSpread(Date date, double spotPrice, float strikeBoundaries) {

    float usedStrike = (long) Math.floor(spotPrice / strikeBoundaries) * strikeBoundaries;
    if (spotPrice - usedStrike > strikeBoundaries / 2) {
      usedStrike = usedStrike + strikeBoundaries;
    }
    float lowerStrike = usedStrike - (niftyStrikeBoundary * spreadStrikes / 10);
    float upperStrike = usedStrike + (niftyStrikeBoundary * spreadStrikes / 10);
    OptionSellSpread optionSellSpread = optionSpreadMap.get(lowerStrike);
    if (optionSellSpread == null) {
      Date expiry = nextExpiry(date, niftyOptionsName, niftyOptionsEs);
      MongoCollection<ZerodhaInstrument> zerodhaInstrumentMongoCollection = getZerodhaInstrumentsCollection();
      ZerodhaInstrument putSell = zerodhaInstrumentMongoCollection.find(Filters.and(
              Filters.eq("name", niftyOptionsName),
              Filters.eq("expiry", expiry),
              Filters.eq("instrumentType", "PE"),
              Filters.eq("strike", lowerStrike)
      )).first();
      ZerodhaInstrument callSell = zerodhaInstrumentMongoCollection.find(Filters.and(
              Filters.eq("name", niftyOptionsName),
              Filters.eq("expiry", expiry),
              Filters.eq("instrumentType", "CE"),
              Filters.eq("strike", upperStrike)
      )).first();
      optionSellSpread = OptionSellSpread.builder()
              .putSell(putSell == null ? null : ZerodhaInstrumentWithPrice.builder()
                      .zerodhaInstrument(putSell)
                      .build())
              .callSell(callSell == null ? null : ZerodhaInstrumentWithPrice.builder()
                      .zerodhaInstrument(callSell)
                      .build())
              .build();
      optionSpreadMap.put(optionSellSpread.getPutSell().getZerodhaInstrument().strike(), optionSellSpread);
    }
    return optionSellSpread;
  }

  private Date nextExpiry(String name, String es) {
    return nextExpiry(new Date(), name, es);
  }

  private Date getFirstOfTheMonth(Date date) {

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    return calendar.getTime();
  }
  private Date nextExpiry(Date date, String name, String es) {
    Map<Date, List<Date>> listOfExpiries = new HashMap<>();
    List<Date> allDates = new ArrayList<>();
    getZerodhaInstrumentsCollection().distinct("expiry",
            Filters.and(Filters.gt("expiry", date),
                    Filters.eq("name", name),
                    Filters.eq("es", es)), Date.class).cursor().forEachRemaining(d -> {
      allDates.add(d);
    });
    Collections.sort(allDates);
    for (Date d : allDates) {

      if (d.after(date)) {
        return d;
      }
    }
    throw new RuntimeException("No expiry found more than four days after today.");
  }
  private Date nextMOnthEndExpiry(Date date, String name, String es) {
    Map<Date, List<Date>> listOfExpiries = new HashMap<>();
    List<Date> allDates = new ArrayList<>();
    getZerodhaInstrumentsCollection().distinct("expiry",
            Filters.and(Filters.gt("expiry", date),
                    Filters.eq("name", name),
                    Filters.eq("es", es)), Date.class).cursor().forEachRemaining(d -> {
      allDates.add(d);
    });
    allDates.forEach(d -> {;
      Date key = getFirstOfTheMonth(d);
      List<Date> expiries = listOfExpiries.get(key);
      if (expiries == null) {
        expiries = new ArrayList<>();
        listOfExpiries.put(key, expiries);
      }
      expiries.add(d);
    });
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    List<Date> expiries = listOfExpiries.get(getFirstOfTheMonth(calendar.getTime()));
    if (expiries != null) {
      Collections.sort(expiries);
      return expiries.getLast();
    }
    else {
      Collections.sort(allDates);
      for (Date e : allDates) {
        if (e.after(date)) {
          return e;
        }
      }
      throw new RuntimeException("No expiry found more than four days after today.");
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
      return 0.0f;
    }
  }
}