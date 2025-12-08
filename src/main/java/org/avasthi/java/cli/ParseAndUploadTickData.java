package org.avasthi.java.cli;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.avasthi.java.cli.pojos.MinuteTick;
import org.avasthi.java.cli.pojos.StockMaster;
import org.bson.Document;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.*;

public class ParseAndUploadTickData extends Base {

  public ParseAndUploadTickData() throws IOException, CsvException {
  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    ParseAndUploadTickData lqr = new ParseAndUploadTickData();
    lqr.update();
  }

  private void update() throws CsvValidationException, IOException {
  }
  private void parseMinuteTicks() {

    Map<Integer, StockMaster> stockMasterMap = new HashMap<>();
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
    System.out.println("Stock Master Map containing instrument token" + stockMasterMap.size());
    File tradeTickDirectory = new File("/data/datasets/Bhavcopy/ticks", "ticks");
    File minuteTickDirectory = new File("/data/datasets/Bhavcopy/ticks", "tick-data");

    Gson gson = new Gson();
    List<MinuteTick> minuteTickList = new ArrayList<>();
    for (File f :minuteTickDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getAbsolutePath().endsWith("json");
      }
    })) {
      int instrument_token = Integer.parseInt(f.getName().split("-")[0]);
      if ( stockMasterMap.containsKey(instrument_token)) {
        StockMaster sm = stockMasterMap.get(instrument_token);
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
    if (minuteTickList.size() > 0) {
      getMinuteTickCollection().insertMany(minuteTickList);
      minuteTickList.clear();
    }
  }
}