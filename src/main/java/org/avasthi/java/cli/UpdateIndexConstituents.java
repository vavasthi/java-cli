package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.StockMaster;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class UpdateIndexConstituents extends Base {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    UpdateIndexConstituents lqr = new UpdateIndexConstituents();
    lqr.update();
  }
  private void update() {
     Set<String> niftyIsin = new HashSet<>(Arrays.asList(
             "INE423A01024",
             "INE742F01042",
             "INE437A01024",
             "INE021A01026",
             "INE238A01034",
             "INE917I01010",
             "INE296A01032",
             "INE918I01026",
             "INE263A01024",
             "INE397D01024",
             "INE059A01026",
             "INE522F01014",
             "INE089A01031",
             "INE066A01021",
             "INE758T01015",
             "INE047A01021",
             "INE860A01027",
             "INE040A01034",
             "INE795G01014",
             "INE038A01020",
             "INE030A01027",
             "INE090A01021",
             "INE154A01025",
             "INE009A01021",
             "INE646L01027",
             "INE019A01038",
             "INE758E01017",
             "INE237A01028",
             "INE018A01030",
             "INE101A01026",
             "INE585B01010",
             "INE027H01010",
             "INE733E01010",
             "INE239A01024",
             "INE213A01029",
             "INE752E01010",
             "INE002A01018",
             "INE123W01016",
             "INE721A01047",
             "INE062A01020",
             "INE044A01036",
             "INE467B01029",
             "INE192A01025",
             "INE155A01022",
             "INE081A01020",
             "INE669C01036",
             "INE280A01028",
             "INE849A01020",
             "INE481G01011",
             "INE075A01022"
     ));
     Set<String> sensexIsin = new HashSet<>(Arrays.asList(
             "INE742F01042",
             "INE021A01026",
             "INE238A01034",
             "INE296A01032",
             "INE918I01026",
             "INE263A01024",
             "INE397D01024",
             "INE758T01015",
             "INE860A01027",
             "INE040A01034",
             "INE030A01027",
             "INE090A01021",
             "INE009A01021",
             "INE154A01025",
             "INE237A01028",
             "INE018A01030",
             "INE101A01026",
             "INE585B01010",
             "INE733E01010",
             "INE752E01010",
             "INE002A01018",
             "INE062A01020",
             "INE044A01036",
             "INE467B01029",
             "INE155A01022",
             "INE081A01020",
             "INE669C01036",
             "INE280A01028",
             "INE1TAE01010",
             "INE849A01020",
             "INE481G01011"
     ));
    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
    Set<String> indexStocks = new HashSet<>(niftyIsin);
    indexStocks.addAll(sensexIsin);
    for (String isin : indexStocks) {
      StockMaster document = collection.find(Filters.eq("isin", isin)).first();
      if (document == null) {
        System.out.println("ISIN doesn't exist " + isin);
      }
      else {
        if (niftyIsin.contains(document.getIsin())) {
          document.setNifty(true);
        }
        if (sensexIsin.contains(document.getIsin())) {
          document.setSensex(true);
        }
        collection.replaceOne(Filters.eq("_id", document.getId()), document);
      }
    }
  }
}