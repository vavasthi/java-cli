package org.avasthi.java.cli;

import com.google.gson.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.avasthi.java.cli.pojos.*;
import org.bson.Document;

import java.io.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ParseAndUploadTickData extends Base {

  private Map<Integer, List<Integer>> alternates = new HashMap<>();
  Map<Integer, StockMaster> stockMasterMap = new HashMap<>();
  public ParseAndUploadTickData() throws IOException, CsvException {
    alternates.put(128028676, Arrays.asList(779521));
    alternates.put(128046084, Arrays.asList(341249));
    alternates.put(128130564, Arrays.asList(2939649));
    alternates.put(128178180, Arrays.asList(356865));
    alternates.put(136236548, Arrays.asList(1270529));
    alternates.put(136308228, Arrays.asList(2714625));
    alternates.put(136330244, Arrays.asList(2953217));
    stockMasterMap.clear();
    getStockMasterCollection().find().forEach(sm -> {
      if (sm.getZerodhaInstrumentToken() != 0) {

        stockMasterMap.put(sm.getZerodhaInstrumentToken(), sm);
      }
    });
    {
      StockMaster bankNifty = new StockMaster();
      bankNifty.setSymbol("BANKNIFTY");
      stockMasterMap.put(260105, bankNifty);
      StockMaster nifty = new StockMaster();
      nifty.setSymbol("NIFTY");
      stockMasterMap.put(256265, nifty);
    }

  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    ParseAndUploadTickData lqr = new ParseAndUploadTickData();
    lqr.parseTradeTicks();
  }

  private void parseMinuteTicks() throws CsvValidationException, IOException {
    System.out.println("Stock Master Map containing instrument token" + stockMasterMap.size());
    File tradeTickDirectory = new File("/data/datasets/Bhavcopy/ticks", "ticks");

    Gson gson = new Gson();
    List<MinuteTick> minuteTickList = new ArrayList<>();
    for (File f :tradeTickDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getAbsolutePath().endsWith("json");
      }
    })) {
      try {

        int instrument_token = Integer.parseInt(f.getName().split("-")[0]);
        StockMaster sm = stockMasterContains(instrument_token);
        if (sm != null) {
          JsonArray ja = gson.fromJson(new FileReader(f), JsonArray.class);
          for (JsonElement je : ja.asList()) {

            JsonObject jo = je.getAsJsonObject();
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(jo.get("date").getAsString());
            MinuteTick mt = new MinuteTick(UUID.randomUUID(),
                    sm.getSymbol(),
                    new Date(offsetDateTime.toInstant().toEpochMilli()),
                    jo.get("open").getAsFloat(),
                    jo.get("high").getAsFloat(),
                    jo.get("low").getAsFloat(),
                    jo.get("close").getAsFloat(),
                    jo.get("volume").getAsFloat());

            minuteTickList.add(mt);
            if (minuteTickList.size() > 1000) {
              getMinuteTickCollection().insertMany(minuteTickList);
              minuteTickList.clear();
            }
          }
        }
        else {
          System.out.println("Instrument Token missing from stock master " + instrument_token);
        }
      }
      catch (NumberFormatException nfe) {
        System.out.println(f.getName());
        throw nfe;
      }
    }
    if (minuteTickList.size() > 0) {
      getMinuteTickCollection().insertMany(minuteTickList);
      minuteTickList.clear();
    }
  }
  private void parseTradeTicks() throws FileNotFoundException {

    System.out.println("Stock Master Map containing instrument token" + stockMasterMap.size());
    File tradeTickDirectory = new File("/data/datasets/Bhavcopy/ticks", "ticks");

    Gson gson = new Gson();
    List<TradeTick> tradeTickList = new ArrayList<>();
    List<TradeTickDepth> tradeTickDepthList = new ArrayList<>();
    final MongoCollection<TradeTick> tradeTickCoillection = getTradeTickCollection();
    final MongoCollection<TradeTickDepth> tradeTickDepthMongoCollection = getTradeTickDepthCollection();

      for (File f :tradeTickDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getAbsolutePath().endsWith("csv");
      }
    })) {
      try {

        JsonArray ja = gson.fromJson(new FileReader(f), JsonArray.class);
        for (JsonElement je : ja.asList()) {

          JsonObject jo = je.getAsJsonObject();
          int instrument_token = jo.get("instrument_token").getAsInt();
          StockMaster sm = stockMasterContains(instrument_token);
          if (sm != null) {

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(jo.get("exchange_timestamp").getAsString(), dtf);
            Date exchangeTimestamp = new Date(ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            System.out.println(exchangeTimestamp);
            boolean tradable = jo.get("tradable").getAsBoolean();
            float lastPrice = jo.get("last_price").getAsFloat();
            float open = jo.get("ohlc").getAsJsonObject().get("open").getAsFloat();
            float high = jo.get("ohlc").getAsJsonObject().get("high").getAsFloat();
            float low = jo.get("ohlc").getAsJsonObject().get("low").getAsFloat();
            float close = jo.get("ohlc").getAsJsonObject().get("close").getAsFloat();
            float change = jo.get("change").getAsFloat();

            if (tradable) {

              float lastTradedQuantity = jo.get("last_traded_quantity").getAsFloat();
              long volumeTraded = jo.get("volume_traded").getAsLong();
              long buyQuantity = jo.get("total_buy_quantity").getAsLong();
              long sellQuantity = jo.get("total_sell_quantity").getAsLong();
              float openInterest = jo.get("oi").getAsFloat();
              float openInterestDayHigh = jo.get("oi_day_high").getAsFloat();
              float openInterestDayLow = jo.get("oi_day_low").getAsFloat();
              TradeTick tt = new TradeTick(UUID.randomUUID(),
                      tradable,
                      sm.getSymbol(),
                      lastPrice,
                      lastTradedQuantity,
                      volumeTraded,
                      buyQuantity,
                      sellQuantity,
                      open,
                      high,
                      low,
                      close,
                      change,
                      openInterest,
                      openInterestDayHigh,
                      openInterestDayLow,
                      exchangeTimestamp);
              JsonArray buyArray = jo.getAsJsonObject("depth").getAsJsonArray("buy");
              JsonArray sellArray = jo.getAsJsonObject("depth").getAsJsonArray("sell");

              List<SingleDepthRecord> buyDepth = new ArrayList<>();
              for (JsonElement depthJe : buyArray.asList()) {
                JsonObject depthJo = depthJe.getAsJsonObject();
                float price = depthJo.get("price").getAsFloat();
                float quantiity = depthJo.get("quantity").getAsFloat();
                int orders = depthJo.get("orders").getAsInt();
                buyDepth.add(new SingleDepthRecord(quantiity, price, orders));
              }
              List<SingleDepthRecord> sellDepth = new ArrayList<>();
              for (JsonElement depthJe : buyArray.asList()) {
                JsonObject depthJo = depthJe.getAsJsonObject();
                float price = depthJo.get("price").getAsFloat();
                float quantiity = depthJo.get("quantity").getAsFloat();
                int orders = depthJo.get("orders").getAsInt();
                sellDepth.add(new SingleDepthRecord(quantiity, price, orders));
              }
              Map<String, List<SingleDepthRecord>> depth = new HashMap<>();
              depth.put("buy", buyDepth);
              depth.put("sell", sellDepth);
              TradeTickDepth ttd = new TradeTickDepth(UUID.randomUUID(), sm.getSymbol(), exchangeTimestamp, depth);
              tradeTickList.add(tt);
              tradeTickDepthList.add(ttd);
            }
            else {

              TradeTick tt = new TradeTick(UUID.randomUUID(),
                      tradable,
                      sm.getSymbol(),
                      lastPrice,
                      0,
                      0,
                      0,
                      0,
                      open,
                      high,
                      low,
                      close,
                      change,
                      0,
                      0,
                      0,
                      exchangeTimestamp);
              tradeTickList.add(tt);
            }
            if (tradeTickList.size() > 1000) {
              tradeTickCoillection.insertMany(tradeTickList);
              if (tradeTickDepthList.size() > 0) {

                tradeTickDepthMongoCollection.insertMany(tradeTickDepthList);
              }
              tradeTickList.clear();
              tradeTickDepthList.clear();
            }
          }
        }
      }
      catch (NumberFormatException nfe) {
        System.out.println(f.getName());
        throw nfe;
      }
      catch (JsonSyntaxException jse) {
        System.out.println(f.getAbsolutePath());
        throw jse;
      }
      catch (NullPointerException npe) {
        System.out.println("Null pointer exception in file" + f.getAbsolutePath());
        throw npe;
      }
      if (tradeTickList.size() > 0) {

        tradeTickCoillection.insertMany(tradeTickList);
        if (tradeTickDepthList.size() > 0) {

          tradeTickDepthMongoCollection.insertMany(tradeTickDepthList);
        }
        tradeTickList.clear();
        tradeTickDepthList.clear();
      }
    }
  }
  private StockMaster stockMasterContains(int instrument_token) {
    if ( stockMasterMap.containsKey(instrument_token)) {
      return stockMasterMap.get(instrument_token);
    }
    else {
      if (alternates.containsKey(instrument_token)) {

        List<Integer> alternateTokens = alternates.get(instrument_token);
        for (int i : alternateTokens) {

          if (stockMasterMap.containsKey(i)) {
            return stockMasterMap.get(i);
          }
        }
      }
      return null;
    }

    }
}