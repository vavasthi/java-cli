package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.avasthi.java.cli.pojos.QuarterlyResults;
import org.avasthi.java.cli.pojos.StockMaster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

public class LoadQuarterlyResults extends QuarterlyResultsBase {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    LoadQuarterlyResults lqr = new LoadQuarterlyResults();
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
    List<QuarterlyResults> documents = new ArrayList<>();
    for (StockMaster sm : collection.find()) {
      File companyDirectory = new File(quarterlyDirectory, sm.getStockCode());
      if (companyDirectory.isDirectory()) {
        for (File html : companyDirectory.listFiles()) {

          parseSingleQuarterlyHtml(html, sm, documents);
          if (documents.size() > 100) {
            qrCollection.insertMany(documents);
            documents.clear();
          }
        }


      }
      if (documents.size() > 0) {
        qrCollection.insertMany(documents);
        documents.clear();
      }
    }
/*      if (html.getName().contains("-")) {
        parseOldHtml(html);
      } else {
        parseNewHtml(html);
      }*/
  }


  private void parseOldHtml(File html){

    System.out.println(String.format("Parsing old file %s", html.getAbsolutePath()));
  }
  private void parseNewHtml (File html) {
    System.out.println(String.format("Parsing new file %s", html.getAbsolutePath()));
  }
}