package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.avasthi.java.cli.pojos.StockMaster;
import org.bson.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class UpdateStockMasterWIthZerodha extends Base {

  private static final Log log = LogFactory.getLog(UpdateStockMasterWIthZerodha.class);

  public UpdateStockMasterWIthZerodha() throws IOException, CsvException {
  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    UpdateStockMasterWIthZerodha lqr = new UpdateStockMasterWIthZerodha();
    lqr.update();
  }

  private void update() throws CsvValidationException, IOException {
    File instrumentsDir = new File("/data/datasets/Bhavcopy", "zerodha");
    File zerodhaInstrumentsCsv = new File(instrumentsDir, "instruments.csv");

    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<StockMaster> collection = getStockMasterCollection();
    CSVReader reader = new CSVReader(new FileReader(zerodhaInstrumentsCsv));
    CSVReader csvReader = new CSVReaderBuilder(new FileReader(zerodhaInstrumentsCsv))
            .withSkipLines(1)
            .build();
    String[] record;

    MongoCollection<StockMaster> stockMasterCollection = getStockMasterCollection();
    while ((record = csvReader.readNext()) != null) {
      int instrumentToken = Integer.parseInt(record[0]);
      String symbol = record[2];
      String segment = record[9];
      String exchange = record[10];
      if (segment.equals("EQ") && exchange.equals("NSE")) {

        try {

          Document filter = new Document("symbol", symbol);
          StockMaster sm = stockMasterCollection.find(filter).first();
          if (sm != null) {

            sm.setZerodhaInstrumentToken(instrumentToken);
            getStockMasterCollection().replaceOne(Filters.eq("_id", sm.getId()), sm);
          }
          else {

            System.out.println(symbol +" is missing from stock master");
          }
        }
        catch (Exception exception) {

        }
      }
    }
  }
}