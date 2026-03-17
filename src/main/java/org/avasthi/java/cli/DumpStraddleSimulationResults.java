package org.avasthi.java.cli;

import com.google.gson.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.avasthi.java.cli.pojos.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DumpStraddleSimulationResults extends Base {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    DumpStraddleSimulationResults lqr = new DumpStraddleSimulationResults();
    lqr.dumpSimulationResults();
  }
  private void dumpSimulationResults() throws FileNotFoundException {

    MongoCollection<SimulatedLongStraddle> simulatedLongStraddleCollection = getMongoClient().getDatabase(database).getCollection(simulatedTradeName, SimulatedLongStraddle.class);
    String header = "id,timestamp,asset,status,strike,spotPrice,profitOpportunities,maxProfit,maxLoss,callSymbol,putSymbol,vix,callBuyIv,putBuyIv,callSellIv,putSellIv,callQuantity,putQuantity," +
            "callBuyPremium,putBuyPremium,callSellPremium,putSellPremium,callBuyVolume,putBuyVolume,callSellVolume,putSellVolume,callBuyTimestamp,putBuyTimestamp,callSellTimestamp, putSellTimestamp," +
            "callBuyOI,putBuyOI,callSellOI,putSellOI";
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    try (final PrintWriter pw = new PrintWriter("dump.csv");) {
      pw.println(header);
      simulatedLongStraddleCollection.find().forEach(s -> {
        String line = String.format("%s,%s,%s,%s,%.2f,%.2f,%d,%.2f,%.2f,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%d,%d,%.2f,%.2f,%.2f,%.2f,%d,%d,%d,%d,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f",
                s.getTradeId(),
                sdf.format(s.getTimestamp()),
                s.getAsset(),
                s.getSell() == null ? "Open" : "Closed",
                s.getStrike(),
                s.getSpotPrice(),
                s.getProfitOpportunityCount(),
                s.getMaxProfit(),
                s.getMaxLoss(),
                s.getBuy().call().getSymbol(),
                s.getBuy().put().getSymbol(),
                s.getVix(),
                s.getBuy().call().getIV(),
                s.getBuy().put().getIV(),
                s.getSell() != null ? s.getSell().call().getIV() : 0,
                s.getSell() != null ? s.getSell().put().getIV() : 0,
                s.getBuy().call().getQuantity(),
                s.getBuy().put().getQuantity(),
                s.getBuy().call().getPremium(),
                s.getBuy().put().getPremium(),
                s.getSell() != null ? s.getSell().call().getPremium() : 0,
                s.getSell() != null ? s.getSell().put().getPremium() : 0,
                s.getBuy().call().getVolumeTraded(),
                s.getBuy().put().getVolumeTraded(),
                s.getSell() != null ? s.getSell().call().getVolumeTraded() : 0,
                s.getSell() != null ? s.getSell().put().getVolumeTraded() : 0,
                sdf.format(s.getBuy().call().getTimestamp()),
                sdf.format(s.getBuy().put().getTimestamp()),
                s.getSell() != null ? sdf.format(s.getSell().call().getTimestamp()) : "",
                s.getSell() != null ? sdf.format(s.getSell().put().getTimestamp()) : "",
                s.getBuy().call().getOpenInterest(),
                s.getBuy().put().getOpenInterest(),
                s.getSell() != null ? s.getSell().call().getOpenInterest() : 0,
                s.getSell() != null ? s.getSell().put().getOpenInterest() : 0);
        pw.println(line);
      });

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}