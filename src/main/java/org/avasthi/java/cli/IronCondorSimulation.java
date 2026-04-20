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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;

public class IronCondorSimulation extends Base {

  private final String asset = "NIFTY";
  private final String niftyOptionsName = "NIFTY";
  private final String niftyOptionsEs = "NSE_OPTIONS";
  private final float niftyStrikeBoundary = 50;

  private final float profitPercentage;
  private final float stopLossPercentage;
  private final MongoCollection<TradeTick> tradeTickMongoCollection = getTradeTickCollection();
  Map<Float, IronCondor> ironCondorMap = new HashMap<>();
  List<IronCondor> finishedIronCondors = new ArrayList<>();
  public IronCondorSimulation(float profitPercentage, float stopLossPercentage) throws IOException, CsvException, ParseException {

    popuateZerodhaInstrumentCollection();
    populateIndiaVix();
    this.profitPercentage = profitPercentage;
    this.stopLossPercentage = stopLossPercentage;
  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    IronCondorSimulation lqr = new IronCondorSimulation(.1f, .1f);
    lqr.run();
  }

  private void run() throws ParseException, IOException {

    MongoCollection<IronCondor> ironCondorMongoCollection = getIronCondorTradesCollection();
    popuateZerodhaInstrumentCollection();
    populateIndiaVix();
    Calendar from = Calendar.getInstance();
    from.set(2026, Calendar.MARCH, 1, 0, 0 ,0);
    Calendar to = Calendar.getInstance();
    to.set(2026, Calendar.APRIL, 18, 0, 0 ,0);
    while (from.before(to)) {
      System.out.println(String.format("Processing for date %s", from.getTime()));
      run(from.get(Calendar.YEAR),
              from.get(Calendar.MONTH),
              from.get(Calendar.DAY_OF_MONTH),
              from.get(Calendar.HOUR_OF_DAY),
              from.get(Calendar.MINUTE),
              from.get(Calendar.SECOND));
      from.add(Calendar.DAY_OF_MONTH, 1);
    }
    int count = 1;
    SimpleDateFormat ddmm = new SimpleDateFormat("ddMMM");
    try (Workbook workbook = new XSSFWorkbook()) {

      for (IronCondor ic : finishedIronCondors) {
        String sheetName = String.format("%s_%d_%d", ddmm.format(ic.getLowerPutSell().getZerodhaInstrument().expiry()),
                (int)(ic.getLowerPutBuy().getZerodhaInstrument().strike()), count++);
        writeIronCondorSheet(workbook, sheetName, ic);
      }
      for (Map.Entry<Float, IronCondor> e : ironCondorMap.entrySet()) {

        IronCondor ic = e.getValue();
        String sheetName = String.format("%s_%d_%d", ddmm.format(ic.getLowerPutSell().getZerodhaInstrument().expiry()),
                (int)(ic.getLowerPutBuy().getZerodhaInstrument().strike()), count++);
        writeIronCondorSheet(workbook, sheetName, ic);
      }
      try (FileOutputStream out = new FileOutputStream("iron-condor.xlsx")) {
        workbook.write(out);
        out.flush();
        out.close();
      }
    }
    catch (Exception e) {}

  }
  private void run(int y, int M, int d, int h, int m, int s) {

    Calendar c = Calendar.getInstance();
    c.set(y, M, d, h, m ,s);
    c.set(Calendar.MILLISECOND, 0);
    Date day = c.getTime();
    SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
    Calendar sod = Calendar.getInstance();
    sod.setTime(day);
    sod.set(Calendar.HOUR_OF_DAY, 9);
    sod.set(Calendar.MINUTE, 30);
    sod.set(Calendar.SECOND, 0);
    sod.set(Calendar.MILLISECOND, 0);
    Calendar eod = Calendar.getInstance();
    eod.setTime(day);
    eod.set(Calendar.HOUR_OF_DAY, 15);
    eod.set(Calendar.MINUTE, 30);
    eod.set(Calendar.SECOND, 0);
    eod.set(Calendar.MILLISECOND, 0);
    Bson spotTradeTickQuery = Filters.and(
            Filters.eq("name", asset),
            Filters.eq("symbol", asset),
            Filters.gte("exchangeTimestamp", sod.getTime()),
            Filters.lte("exchangeTimestamp", eod.getTime())
    );
    MongoCursor<TradeTick> cursor = getTradeTickCollection().find(spotTradeTickQuery).sort(Sorts.ascending("exchangeTimestamp")).cursor();
    if (cursor.hasNext()) {
      while(cursor.hasNext()) {
        TradeTick spotTradeTick = cursor.next();
        IronCondor ironCondor = ironCondor(spotTradeTick.exchangeTimestamp(), spotTradeTick.lastPrice(), niftyStrikeBoundary);
        if (ironCondor.isValid()) {

          TradeTick lowerPutBuyTradeTick = tradeTickMongoCollection.find(Filters.and(
                  Filters.eq("symbol", ironCondor.getLowerPutBuy().getZerodhaInstrument().symbol()),
                  Filters.eq("exchangeTimestamp", spotTradeTick.exchangeTimestamp())
          )).first();
          TradeTick lowerPutSellTradeTick = tradeTickMongoCollection.find(Filters.and(
                  Filters.eq("symbol", ironCondor.getLowerPutSell().getZerodhaInstrument().symbol()),
                  Filters.eq("exchangeTimestamp", spotTradeTick.exchangeTimestamp())
          )).first();
          TradeTick upperCallSellTradeTick = tradeTickMongoCollection.find(Filters.and(
                  Filters.eq("symbol", ironCondor.getUpperCallSell().getZerodhaInstrument().symbol()),
                  Filters.eq("exchangeTimestamp", spotTradeTick.exchangeTimestamp())
          )).first();
          TradeTick upperCallBuyTradeTick = tradeTickMongoCollection.find(Filters.and(
                  Filters.eq("symbol", ironCondor.getUpperCallBuy().getZerodhaInstrument().symbol()),
                  Filters.eq("exchangeTimestamp", spotTradeTick.exchangeTimestamp())
          )).first();
          if (lowerPutBuyTradeTick != null && lowerPutSellTradeTick != null && upperCallSellTradeTick != null && upperCallBuyTradeTick != null) {
            IronCondor.STATUS status
                    = ironCondor.updateTicks(lowerPutBuyTradeTick, lowerPutSellTradeTick, upperCallSellTradeTick, upperCallBuyTradeTick, spotTradeTick, profitPercentage, stopLossPercentage);
            if (status != IronCondor.STATUS.RUNNING) {
              ironCondorMap.remove(ironCondor.getLowerPutBuy().getZerodhaInstrument().strike());
              finishedIronCondors.add(ironCondor);
            }
          }
        }
      }
    }
  }


  private void writeIronCondorSheet(Workbook workbook, String sheetName, IronCondor ironCondor) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Sheet sheet = workbook.getSheet(sheetName);
    if (sheet == null) {
      sheet = workbook.createSheet(sheetName);
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Expiry");
      header.createCell(1).setCellValue("Timestamp");
      header.createCell(2).setCellValue("Spot Price");
      header.createCell(3).setCellValue("Put Buy Strike");
      header.createCell(4).setCellValue("Put Buy Price");
      header.createCell(5).setCellValue("Put Buy Lot");
      header.createCell(6).setCellValue("Put Sell Strike");
      header.createCell(7).setCellValue("Put Sell Price");
      header.createCell(8).setCellValue("Put Sell Lot");
      header.createCell(9).setCellValue("Call Sell Strike");
      header.createCell(10).setCellValue("Call Sell Price");
      header.createCell(11).setCellValue("Call Sell Lot");
      header.createCell(12).setCellValue("Call Buy Strike");
      header.createCell(13).setCellValue("Call Buy Price");
      header.createCell(14).setCellValue("Call Buy Lot");
      header.createCell(15).setCellValue("Profit");
      header.createCell(16).setCellValue("Status");
    }
    Date expiry = ironCondor.getLowerPutSell().getZerodhaInstrument().expiry();
    for (IronCondor.TradeTicks tt : ironCondor.getOtherTicks()) {

      Row data = sheet.createRow(sheet.getLastRowNum() + 1);
      data.createCell(0).setCellValue(formatter.format(expiry));;
      data.createCell(1).setCellValue(formatter.format(tt.lowerPutBuyTradeTick().exchangeTimestamp()));
      data.createCell(2).setCellValue(tt.spotTradeTick().lastPrice());
      data.createCell(3).setCellValue(ironCondor.getLowerPutBuy().getZerodhaInstrument().strike());;
      data.createCell(4).setCellValue(tt.lowerPutBuyTradeTick().lastPrice());;
      data.createCell(5).setCellValue(ironCondor.getLowerPutBuy().getZerodhaInstrument().lotSize());;
      data.createCell(6).setCellValue(ironCondor.getLowerPutSell().getZerodhaInstrument().strike());;
      data.createCell(7).setCellValue(tt.lowerPutSellTradeTick().lastPrice());;
      data.createCell(8).setCellValue(ironCondor.getLowerPutSell().getZerodhaInstrument().lotSize());;
      data.createCell(9).setCellValue(ironCondor.getUpperCallSell().getZerodhaInstrument().strike());;
      data.createCell(10).setCellValue(tt.upperCallSellTradeTick().lastPrice());;
      data.createCell(11).setCellValue(ironCondor.getUpperCallSell().getZerodhaInstrument().lotSize());;
      data.createCell(12).setCellValue(ironCondor.getUpperCallBuy().getZerodhaInstrument().strike());;
      data.createCell(13).setCellValue(tt.upperCallBuyTradeTick().lastPrice());;
      data.createCell(14).setCellValue(ironCondor.getUpperCallBuy().getZerodhaInstrument().lotSize());;
      data.createCell(15).setCellValue(ironCondor.computeProfit(tt));;
      data.createCell(16).setCellValue(tt.status().getStatus());;
    }
  }

  private IronCondor ironCondor(Date date, double spotPrice, float strikeBoundaries) {

    float usedStrike = (long) Math.floor(spotPrice / strikeBoundaries) * strikeBoundaries;
    if (spotPrice - usedStrike > strikeBoundaries / 2) {
      usedStrike = usedStrike + strikeBoundaries;
    }
    IronCondor ironCondor = ironCondorMap.get(usedStrike  - strikeBoundaries * 8);
    if (ironCondor == null) {
      Date expiry = nextExpiry(date, niftyOptionsName, niftyOptionsEs);
      MongoCollection<ZerodhaInstrument> zerodhaInstrumentMongoCollection = getZerodhaInstrumentsCollection();
      ZerodhaInstrument lowerPutBuy = zerodhaInstrumentMongoCollection.find(Filters.and(
              Filters.eq("name", niftyOptionsName),
              Filters.eq("expiry", expiry),
              Filters.eq("instrumentType", "PE"),
              Filters.eq("strike", usedStrike - strikeBoundaries * 8)
      )).first();
      ZerodhaInstrument lowerPutSell = zerodhaInstrumentMongoCollection.find(Filters.and(
              Filters.eq("name", niftyOptionsName),
              Filters.eq("expiry", expiry),
              Filters.eq("instrumentType", "PE"),
              Filters.eq("strike", usedStrike - strikeBoundaries * 4)
      )).first();
      ZerodhaInstrument upperCallSell = zerodhaInstrumentMongoCollection.find(Filters.and(
              Filters.eq("name", niftyOptionsName),
              Filters.eq("expiry", expiry),
              Filters.eq("instrumentType", "CE"),
              Filters.eq("strike", usedStrike + strikeBoundaries * 4)
      )).first();
      ZerodhaInstrument upperCallBuy = zerodhaInstrumentMongoCollection.find(Filters.and(
              Filters.eq("name", niftyOptionsName),
              Filters.eq("expiry", expiry),
              Filters.eq("instrumentType", "CE"),
              Filters.eq("strike", usedStrike + strikeBoundaries * 8)
      )).first();
      ironCondor = IronCondor.builder()
              .lowerPutBuy(lowerPutBuy == null ? null : ZerodhaInstrumentWithPrice.builder()
                      .zerodhaInstrument(lowerPutBuy)
                      .build())
              .lowerPutSell(lowerPutSell == null ? null : ZerodhaInstrumentWithPrice.builder()
                      .zerodhaInstrument(lowerPutSell)
                      .build())
              .upperCallSell(upperCallSell == null ? null : ZerodhaInstrumentWithPrice.builder()
                      .zerodhaInstrument(upperCallSell)
                      .build())
              .upperCallBuy(upperCallBuy == null ? null : ZerodhaInstrumentWithPrice.builder()
                      .zerodhaInstrument(upperCallBuy)
                      .build())
              .build();
      ironCondorMap.put(ironCondor.getLowerPutBuy().getZerodhaInstrument().strike(), ironCondor);
    }
    return ironCondor;
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
    if (calendar.get(Calendar.DAY_OF_MONTH) > 18) {

      calendar.add(Calendar.MONTH, 1);
    }
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