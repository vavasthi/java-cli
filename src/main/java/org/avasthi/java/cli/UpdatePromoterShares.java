package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.QuarterlyResults;
import org.avasthi.java.cli.pojos.StockMaster;
import org.bson.Document;

import java.io.IOException;
import java.text.ParseException;

public class UpdatePromoterShares extends Base {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    UpdatePromoterShares lqr = new UpdatePromoterShares();
    lqr.update();
  }
  private void update() {

    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
    MongoCollection<QuarterlyResults> qrCollection = db.getCollection(quarterlyResultsCollectionName, QuarterlyResults.class);
    for (StockMaster sm : collection.find()) {
      float oldPromoterShares = 0;
      float oldNonPromoterShares = 0;
      float oldPromoterPercentageShares = 0;
      for (QuarterlyResults qr : qrCollection.find(Filters.and(Filters.eq("companyId", sm.getId()))).sort(Filters.eq("periodStarting", 1))) {

        boolean shouldUpdate = false;
        if (qr.getPromoterShares() == 0 && oldPromoterShares != 0) {
          qr.setPromoterShares(oldPromoterShares);
          shouldUpdate = true;
        }
        if (qr.getNonPromoterShares() == 0 && oldNonPromoterShares != 0) {
          qr.setNonPromoterShares(oldNonPromoterShares);
          shouldUpdate = true;
        }
        if (qr.getPromoterPercentageOfShares() == 0 && oldPromoterPercentageShares != 0) {
          qr.setPromoterPercentageOfShares(oldPromoterPercentageShares);
          shouldUpdate = true;
        }
        qrCollection.replaceOne(Filters.eq("_id", qr.getId()), qr);
        oldPromoterShares = qr.getPromoterShares();
        oldNonPromoterShares = qr.getNonPromoterShares();
        oldPromoterPercentageShares = qr.getPromoterPercentageOfShares();
      }
    }
  }
}