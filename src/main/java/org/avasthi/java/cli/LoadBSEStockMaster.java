package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.StockMaster;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;


public class LoadBSEStockMaster extends Base{
    int[] codes = {
            500034,
            511218
};


    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        LoadBSEStockMaster lsm = new LoadBSEStockMaster();

        WebDriver driver = lsm.getWebDriver();
//        lsm.convertOldRecords();
        lsm.loadMissingStuff(driver);
       // lsm.updateDocumentId();
    }
    private void convertOldRecords() throws InterruptedException {

        MongoDatabase db = getMongoClient().getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(stockMasterCollectionName, Document.class);
        MongoCollection<StockMaster> smCollection = db.getCollection(stockMasterCollectionName, StockMaster.class);
        for (Document d : collection.find(Document.parse(new String("{securityCode:{$exists:true}}")))) {
            StockMaster stockMaster = new StockMaster(UUID.randomUUID(),
                    d.getString("symbol"),
                    d.getString("name"),
                    d.getString("series"),
                    d.getDate("dateOfListing"),
                    d.getDouble("paidUpValue"),
                    d.getInteger("marketLot"),
                    d.getString("isin"),
                    d.getDouble("faceValue"),
                    d.getString("securityCode"));
            collection.deleteOne(d);
            smCollection.insertOne(stockMaster);
            collection.deleteOne(d);
        }
    }
    private void updateDocumentId() {

        MongoDatabase db = getMongoClient().getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(stockMasterCollectionName, Document.class);
        for (Document doc : collection.find() ) {
            Document newDocument = new Document(doc);
            newDocument.replace("_id", UUID.randomUUID());
            collection.deleteOne(Filters.eq("_id", doc.get("_id")));
            collection.insertOne(newDocument);
        }
    }

    private void loadMissingStuff(WebDriver driver) throws InterruptedException {

        MongoDatabase db = getMongoClient().getDatabase(database);
        MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
        for (int code :codes) {

            driver.get("https://www.bseindia.com/getquote.aspx");
            WebElement element = driver.findElement(By.xpath("/html/body/form/div[4]/div/div/div[2]/div/div/div/div[2]/div/table/tbody/tr/td/div/div/input"));
            element.sendKeys(String.format("%d", code));
            Thread.sleep(5000);
            for (WebElement webElement : driver.findElements(By.xpath("/html/body/form/div[4]/div/div/div[2]/div/div/div/div[2]/div/table/tbody/tr/td/div/div/div/ul/li"))) {
                String[] texts = webElement.getText().split("\n");
                if (texts.length > 1) {
                    String name = texts[0];
                    String[] t2 = texts[1].replaceAll("\\s+", " ").split(" ");
                    String symbol = t2[0];
                    String isin = t2[1];
                    String securityCode = t2[2];
                    System.out.printf("%s,%s,%s,%s\n", name, isin, symbol, securityCode);
                    StockMaster stockMaster = new StockMaster(UUID.randomUUID(), symbol, name, "", new Date(0), 0, 0, isin, 0, securityCode);
                    StockMaster storedStockMaster = collection.find(Filters.eq("symbol", stockMaster.getSymbol())).first();
                    if (storedStockMaster == null) {
                        collection.insertOne(stockMaster);
                    }
                    else {
                        storedStockMaster.setIsin(stockMaster.getIsin());
                        storedStockMaster.setName(stockMaster.getName());
                        storedStockMaster.setSymbol(stockMaster.getSymbol());
                        if (storedStockMaster.getId() == null) {
                            System.out.println(String.format("Deleting document with symbol = %s", storedStockMaster.toString()));
                            collection.deleteMany(Filters.eq("symbol", storedStockMaster.getSymbol()));
                            storedStockMaster.setId(UUID.randomUUID());
                            collection.insertOne(storedStockMaster);
                        }
                        else {

                            collection.replaceOne(Filters.eq("_id", storedStockMaster.getId()), storedStockMaster);
                        }
                    }
                }
                else {
                    System.out.println(texts[0]);
                }
            }
        }
    }
}