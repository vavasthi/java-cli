package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.QuarterlyResults;
import org.avasthi.java.cli.pojos.StockMaster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

public class LoadMissingQuarterlyResults extends QuarterlyResultsBase {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    LoadMissingQuarterlyResults lqr = new LoadMissingQuarterlyResults();
    lqr.parse();
  }

  private void parse() throws IOException {

    File quarterlyDirectory = new File("/data/datasets/quarterly");
    File outFile = new File("texts.txt");
    System.out.println(outFile.getAbsolutePath());
    PrintWriter pw =  new PrintWriter(new FileWriter(outFile));
    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
    MongoCollection<QuarterlyResults> qrCollection = db.getCollection(quarterlyResultsCollectionName, QuarterlyResults.class);
    List<QuarterlyResults> quarterlyResults = new ArrayList<>();
    for (StockMaster sm : collection.find(Filters.or(Filters.eq("sensex", true), Filters.eq("nifty50", true)))) {
      Date previousEnd = null;
      QuarterlyResults oldQr = null;
      for (QuarterlyResults qr : qrCollection.find(Filters.eq("isin", sm.getIsin())).sort(Filters.eq("periodStarting", -1))) {
        if (previousEnd != null) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(previousEnd);
          cal.add(Calendar.DAY_OF_MONTH, 1);
          if (cal.getTime().equals(qr.getPeriodStarting())) {

          }
          else if (oldQr.getPeriodStarting().equals(qr.getPeriodStarting())) {

            qrCollection.deleteOne(Filters.eq("_id", oldQr.getId()));
          }
          else {
            Calendar periodEnding = Calendar.getInstance();
            periodEnding.setTime(qr.getPeriodEnding());
            File file = new File("/data/datasets/quarterly", String.format("%s/%s_%02d%02d_Q_Standalone.html", qr.getStockCode(), qr.getStockCode(), periodEnding.get(Calendar.MONTH) + 1, periodEnding.get(Calendar.YEAR)%100));
            if (!file.canRead()) {
              file = new File("/data/datasets/quarterly", String.format("%s/%s_%02d%02d_Q_Consolidated.html", qr.getStockCode(), qr.getStockCode(), periodEnding.get(Calendar.MONTH) + 1, periodEnding.get(Calendar.YEAR)%100));
            }
            try {

              parseSingleQuarterlyHtml(file, sm, quarterlyResults);
            }
            catch(Exception e) {
              System.out.println("Results file not found " + file.getAbsolutePath() + e.toString());
            }
          }
        }
        previousEnd = qr.getPeriodEnding();
        oldQr = qr;
        if (quarterlyResults.size() > 100) {
          qrCollection.insertMany(quarterlyResults);
          quarterlyResults.clear();;
        }
      }
    }
    if (quarterlyResults.size() > 0) {
      qrCollection.insertMany(quarterlyResults);
    }
  }
}