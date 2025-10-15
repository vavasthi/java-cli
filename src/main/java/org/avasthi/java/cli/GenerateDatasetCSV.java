package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.*;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GenerateDatasetCSV extends QuarterlyResultsBase {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    GenerateDatasetCSV lqr = new GenerateDatasetCSV();
    lqr.parse();
  }

  private void parse() throws IOException {

    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
    MongoCollection<StockPrice> spCollection = db.getCollection(stockPriceCollectionName, StockPrice.class);
    MongoCollection<QuarterlyResults> qrCollection = db.getCollection(quarterlyResultsCollectionName, QuarterlyResults.class);
    MongoCollection<CorporateEvent> ceCollection = db.getCollection(corporateEventCollectionName, CorporateEvent.class);
    MongoCollection<Cpi> cpiCollection = db.getCollection(cpiCollectionName, Cpi.class);
    MongoCollection<Iip> iipCollection = db.getCollection(iipCollectionName, Iip.class);
    List<QuarterlyResults> documents = new ArrayList<>();
    for (StockMaster sm : collection.find(Filters.or(Filters.eq("nifty50", true), Filters.eq("sensex", true)))) {
      Calendar calendar = Calendar.getInstance();
      for (StockPrice sp : spCollection.find(Filters.eq("symbol", sm.getSymbol())).sort(Filters.eq("date", 1))) {
        float bonus = 0;
        float dividend = 0;
        calendar.setTime(sp.date());
        CorporateEvent ce  = ceCollection.find(Filters.and(Filters.exists("bonus"), Filters.eq("symbol", sp.symbol()), Filters.eq("date", sp.date()))).first();
        if (ce != null) {
          bonus = ce.bonus();
        }
        ce  = ceCollection.find(Filters.and(Filters.exists("dividend"), Filters.eq("symbol", sp.symbol()), Filters.eq("date", sp.date()))).first();
        if (ce != null) {
          dividend = ce.dividend();
        }
        float eps = 0;
        float equity = 0;
        float pbt = 0;
        float pat = 0;
        float tax = 0;
        float promoterShares = 0;
        float nonPromoterShares = 0;
        Bson dateFilter = Filters.and(Filters.eq("companyId", sm.getId()), Filters.gte("periodStarting", sp.date()));
        QuarterlyResults qr = qrCollection.find(dateFilter).sort(Filters.eq("periodStarting", 1)).first();
        if (qr != null) {
          eps = qr.getEps();
          equity = qr.getEquity();
          pbt = qr.getProfitBeforeTax();
          pat = qr.getProfitAfterTax();
          tax = qr.getTax();
          promoterShares = qr.getPromoterShares();
          nonPromoterShares = qr.getNonPromoterShares();
        }
        else {

          System.out.println("Date filters = " + dateFilter.toString());
        }
        System.out.printf("%s,%d,%d,%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f\n", sm.getSymbol(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), sp.open(), sp.close(), sp.high(), sp.low(), sp.adjustedClose(), (float)sp.volume(), bonus, dividend, eps,equity,pbt,pat,tax,promoterShares,nonPromoterShares);
      }
    }
  }

}