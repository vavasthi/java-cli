package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.StockMaster;
import org.bson.Document;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UpdateStockPriceTypes extends Base {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    UpdateStockPriceTypes lqr = new UpdateStockPriceTypes();
    lqr.update();
  }
  private void update() {

    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<Document> collection = db.getCollection(stockPriceCollectionName, Document.class);
    for (Document d : collection.find()) {
      if (!d.containsKey("volume")) {
        d.append("volume", 0f);
        collection.replaceOne(Filters.eq("_id", d.get("_id")), d);
      }
    }
  }
}