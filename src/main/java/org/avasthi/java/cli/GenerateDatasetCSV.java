package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.*;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GenerateDatasetCSV extends QuarterlyResultsBase {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    GenerateDatasetCSV lqr = new GenerateDatasetCSV();
    lqr.parse();
  }

  private void parse() throws IOException {

    SimpleDateFormat ddmmyy = new SimpleDateFormat("dd/MM/yyyy");
    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
    MongoCollection<StockPrice> spCollection = db.getCollection(stockPriceCollectionName, StockPrice.class);
    MongoCollection<QuarterlyResults> qrCollection = db.getCollection(quarterlyResultsCollectionName, QuarterlyResults.class);
    MongoCollection<CorporateEvent> ceCollection = db.getCollection(corporateEventCollectionName, CorporateEvent.class);
    MongoCollection<Cpi> cpiCollection = db.getCollection(cpiCollectionName, Cpi.class);
    MongoCollection<Iip> iipCollection = db.getCollection(iipCollectionName, Iip.class);
    List<QuarterlyResults> documents = new ArrayList<>();
    for (StockMaster sm : collection.find(Filters.or(Filters.eq("nifty50", true), Filters.eq("sensex", true)))) {
      File outdir = new File("outputs");
      outdir.mkdirs();
      File csv = new File(outdir,String.format("%s.csv", sm.getSymbol()));
      PrintWriter pw = new PrintWriter(csv);
      pw.printf("StockCode,Year,Month,Day,Open,Close,High,Low,AdjustedClose,Volume,Bonus,Dividend,EPS,Equity,PBT,PAT,Tax,PromoterShares,NonPromoterShares,cpiOverall,cpiHousing,cpiFuel,cpiVegetables,cpiGeneral,iipBasicGoods,iipCapitalGoods,iipConsumerDurables,iipElectricity,iipIntermediateGoods,iipGeneral,iipOtherManufacturing\n");
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
        Calendar c = Calendar.getInstance();
        c.setTime(sp.date());
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date sDate = c.getTime();
        c.set(Calendar.HOUR, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        Date eDate = c.getTime();
        Bson dateFilter = Filters.and(Filters.eq("companyId", sm.getId()), Filters.gte("broadcastTime", sDate), Filters.lte("broadcastTime", eDate));
        Cpi cpi = cpiCollection.find(Filters.and(Filters.eq("state", "All India"), Filters.eq("month", c.get(Calendar.MONTH)), Filters.eq("year", c.get(Calendar.YEAR)))).first();
        float cpiOverall = 0;
        float cpiHousing = 0;
        float cpiFuel = 0;
        float cpiVegetables = 0;
        float cpiGeneral = 0;
        if (cpi != null) {
          cpiOverall = cpi.getMiscellaneousOverall();
          cpiHousing = cpi.getHousingOverall();
          cpiFuel = cpi.getFuelAndLightOverall();
          cpiVegetables = cpi.getVegetables();
          cpiGeneral = cpi.getGeneralOverall();
        }
        Iip iip = iipCollection.find(Filters.and(Filters.eq("month", c.get(Calendar.MONTH)), Filters.eq("year", c.get(Calendar.YEAR)))).first();
        float iipBasicGoods = 0;
        float iipCapitalGoods = 0;
        float iipConsumerDurables = 0;
        float iipElectricity = 0;
        float iipIntermediateGoods = 0;
        float iipGeneral = 0;
        float iipOtherManufacturing = 0;
        if (iip != null) {

          iipBasicGoods = iip.getBasicGoods();
          iipCapitalGoods = iip.getCapitalGoods();
          iipConsumerDurables = iip.getConsumerDurables();
          iipElectricity = iip.getElectricity();
          iipIntermediateGoods = iip.getIntermediateGoods();
          iipGeneral = iip.getGeneral();
          iipOtherManufacturing = iip.getOtherManufacturing();
        }
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

        }
        pw.printf("%s,%d,%d,%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f\n",
                sm.getStockCode(),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                sp.open(),
                sp.close(),
                sp.high(),
                sp.low(),
                sp.adjustedClose(),
                (float)sp.volume(),
                bonus,
                dividend,
                eps,
                equity,
                pbt,
                pat,
                tax,
                promoterShares,
                nonPromoterShares,
                cpiOverall,
                cpiHousing,
                cpiFuel,
                cpiVegetables,
                cpiGeneral,
                iipBasicGoods,
                iipCapitalGoods,
                iipConsumerDurables,
                iipElectricity,
                iipIntermediateGoods,
                iipGeneral,
                iipOtherManufacturing);


      }
      pw.close();
    }
  }

}