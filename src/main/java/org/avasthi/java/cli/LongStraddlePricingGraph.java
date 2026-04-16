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
import org.avasthi.java.cli.pojos.DailyVIX;
import org.avasthi.java.cli.pojos.OptionPair;
import org.avasthi.java.cli.pojos.TradeTick;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;
import org.bson.conversions.Bson;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;

public class LongStraddlePricingGraph extends Base {

  ExecutorService executorService = Executors.newFixedThreadPool(20);

  private final String asset = "NIFTY";
  private final String niftyOptionsName = "NIFTY";
  private final String niftyOptionsEs = "NSE_OPTIONS";

  public LongStraddlePricingGraph() throws IOException, CsvException, ParseException {

    popuateZerodhaInstrumentCollection();
    populateIndiaVix();
    Calendar from = Calendar.getInstance();
    from.set(2025, Calendar.DECEMBER, 8, 0, 0 ,0);
    Calendar to = Calendar.getInstance();
    to.set(2026, Calendar.APRIL, 8, 0, 0 ,0);
  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    LongStraddlePricingGraph lqr = new LongStraddlePricingGraph();
    lqr.pricingGraphData();
  }

  private void pricingGraphData() throws ParseException, FileNotFoundException {

    popuateZerodhaInstrumentCollection();
    populateIndiaVix();
    Calendar from = Calendar.getInstance();
    from.set(2026, Calendar.MARCH, 20, 0, 0 ,0);
    Calendar to = Calendar.getInstance();
    to.set(2026, Calendar.APRIL, 17, 0, 0 ,0);
    while (from.before(to)) {
          pricingGraphData(from.get(Calendar.YEAR),
                  from.get(Calendar.MONTH),
                  from.get(Calendar.DAY_OF_MONTH),
                  from.get(Calendar.HOUR_OF_DAY),
                  from.get(Calendar.MINUTE),
                  from.get(Calendar.SECOND));
      from.add(Calendar.DAY_OF_MONTH, 1);
    }
  }
  private void pricingGraphData(int y, int M, int d, int h, int m, int s) {

    Map<Float, OptionPair> optionMap = new HashMap<>();
    Map<String, ZerodhaInstrument> zerodhaInstrumentMap = new HashMap<>();
    Calendar c = Calendar.getInstance();
    c.set(y, M, d, h, m ,s);
    c.set(Calendar.MILLISECOND, 0);
    Date day = c.getTime();
    Set<Float> usedStrikes = new HashSet<>();
    System.out.println("Processing for " + day);
    SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Calendar sod = Calendar.getInstance();
    sod.setTime(day);
    sod.set(Calendar.HOUR_OF_DAY, 9);
    sod.set(Calendar.MINUTE, 15);
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
    SimpleDateFormat ddmm = new SimpleDateFormat("ddMMM");
    if (cursor.hasNext()) {
      try (Workbook workbook = new XSSFWorkbook()) {
        while(cursor.hasNext()) {
          TradeTick spotTradeTick = cursor.next();
          Date expiry = nextExpiry(sod.getTime(), asset, niftyOptionsEs);
          populateOptionPairMap(optionMap, zerodhaInstrumentMap, expiry);

          usedStrikes.addAll(findNearestStrike(spotTradeTick.lastPrice(), 50));
          usedStrikes.forEach(strike -> {
            String sheetName = String.format("%s-%.0f", ddmm.format(expiry), strike);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
              sheet = workbook.createSheet(sheetName);
              Row header = sheet.createRow(0);
              header.createCell(0).setCellValue("Expiry");
              header.createCell(1).setCellValue("Timestamp");
              header.createCell(2).setCellValue("Strike");
              header.createCell(3).setCellValue("Lot Size");
              header.createCell(4).setCellValue("Call Price");
              header.createCell(5).setCellValue("Put Price");
              header.createCell(6).setCellValue("Total Price");
              header.createCell(7).setCellValue("SpotPrice");
              header.createCell(8).setCellValue("CallIV");
              header.createCell(9).setCellValue("PutIV");
            }
            OptionPair op = optionMap.get(strike);
            TradeTick callTradeTick = getTradeTickCollection().find(Filters.and(
                    Filters.eq("symbol", op.call().symbol()),
                    Filters.eq("exchangeTimestamp", spotTradeTick.exchangeTimestamp())
            )).first();
            TradeTick putTradeTick = getTradeTickCollection().find(Filters.and(
                    Filters.eq("symbol", op.put().symbol()),
                    Filters.eq("exchangeTimestamp", spotTradeTick.exchangeTimestamp())
            )).first();
            if (callTradeTick != null && putTradeTick != null) {

              double callIV = ImpliedVolatility.calculateIV(callTradeTick.lastPrice(), spotTradeTick.lastPrice(), strike, spotTradeTick.exchangeTimestamp(), expiry, 0.10, true);
              double putIV = ImpliedVolatility.calculateIV(putTradeTick.lastPrice(), spotTradeTick.lastPrice(), strike, spotTradeTick.exchangeTimestamp(), expiry, 0.10, false);
              Row data = sheet.createRow(sheet.getLastRowNum() + 1);
              data.createCell(0).setCellValue(formatter.format(op.call().expiry()));
              data.createCell(1).setCellValue(formatter.format(spotTradeTick.exchangeTimestamp()));;
              data.createCell(2).setCellValue(strike);;
              data.createCell(3).setCellValue(op.call().lotSize());;
              data.createCell(4).setCellValue(callTradeTick.lastPrice());;
              data.createCell(5).setCellValue(putTradeTick.lastPrice());;
              data.createCell(6).setCellValue((callTradeTick.lastPrice() + putTradeTick.lastPrice())*op.call().lotSize());;
              data.createCell(7).setCellValue(spotTradeTick.lastPrice());;
              data.createCell(8).setCellValue(callIV);;
              data.createCell(9).setCellValue(putIV);;
            }
          });
        }
        try (FileOutputStream out = new FileOutputStream("daily-pricing-" + yyyymmdd.format(day) + ".xlsx")) {
          workbook.write(out);
          out.flush();
          out.close();
        }
      }
      catch (Exception e) {
      }
    }
  }

  private void populateOptionPairMap(Map<Float, OptionPair> optionMap,
                                     Map<String, ZerodhaInstrument> zerodhaInstrumentMap,
                                     Date expiry) {

    optionMap.clear();
    MongoCollection<ZerodhaInstrument> zerodhaInstrumentMongoCollection = getZerodhaInstrumentsCollection();
    Bson instrumentFilter = Filters.and(
            Filters.eq("name", niftyOptionsName),
            Filters.eq("es", "NSE_OPTIONS"),
            Filters.eq("expiry", expiry)
    );
    zerodhaInstrumentMongoCollection.find(instrumentFilter).forEach(zi -> {
      zerodhaInstrumentMap.put(zi.symbol(), zi);
      OptionPair op = optionMap.get(zi.strike());
      if (op == null) {
        optionMap.put(zi.strike(), new OptionPair(zi.instrumentType().equals("CE") ? zi : null, zi.instrumentType().equals("PE") ? zi : null));
      } else {
        optionMap.put(zi.strike(), new OptionPair(zi.instrumentType().equals("CE") ? zi : op.call(), zi.instrumentType().equals("PE") ? zi : op.put()));
      }
    });
  }

  private Set<Float> findNearestStrike(double spotPrice, int expiryBoundaries) {
    float lowerExpiry = (long) Math.floor(spotPrice / expiryBoundaries) * 50;
    float upperExpiry = lowerExpiry + expiryBoundaries;
    return Arrays.asList(lowerExpiry, upperExpiry).stream().collect(Collectors.toSet());
  }

  private Date nextExpiry(String name, String es) {
    return nextExpiry(new Date(), name, es);
  }

  private Date nextExpiry(Date date, String name, String es) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.HOUR_OF_DAY, 0);
    calendar.add(Calendar.MINUTE, 0);
    calendar.add(Calendar.SECOND, 0);
    calendar.add(Calendar.MILLISECOND, 0);
    date = calendar.getTime();
    long fourDaysNumberOfMilliseconds = 4 * 24 * 60 * 60 * 1000;
    List<Date> dates = new ArrayList<>();
    getZerodhaInstrumentsCollection().distinct("expiry",
            Filters.and(Filters.gt("expiry", date),
                    Filters.eq("name", name),
                    Filters.eq("es", es)), Date.class).cursor().forEachRemaining(d -> {
      dates.add(d);
    });
    Collections.sort(dates);
    for (Date e : dates) {
      if (e.getTime() - date.getTime() > fourDaysNumberOfMilliseconds) {
        return e;
      }
    }
    throw new RuntimeException("No expiry found more than four days after today.");
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