package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import org.avasthi.java.cli.pojos.CorporateEvent;
import org.avasthi.java.cli.pojos.Currency;
import org.avasthi.java.cli.pojos.StockMaster;
import org.avasthi.java.cli.pojos.StockPrice;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadCurrency extends Base {
  private String mongoUrl = "mongodb://localhost";
  public static void main(String[] args) throws IOException, InterruptedException {

    DownloadCurrency dbc = new DownloadCurrency();
    dbc.downloadCurrency();
  }
  private void downloadCurrency() throws InterruptedException, IOException {
    WebDriver driver = getChromeDriver(true);

    File dataDirectory = new File("/data/datasets/stockPredictor/currency");
    final List<Currency> currencyList = new ArrayList<>();
    for (File f : dataDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().contains("inr-");
      }
    })) {
      DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
              .parseCaseInsensitive()
              .parseLenient()
              .appendPattern("dd/MM/yyyy")
              .toFormatter(Locale.ENGLISH);
      ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
      System.out.println(f.getAbsolutePath());
      driver.get("file://" + f.getAbsolutePath());
      String xpath = "/html/body/table/tbody/tr[%d]/td[%d]";
      int count = 2;
      try {
        while (true) {

          String dateXpath = String.format(xpath, count, 1);
          String usdXpath = String.format(xpath, count, 2);
          String gbpXpath = String.format(xpath, count, 3);
          String euroXpath = String.format(xpath, count, 4);
          String yenXpath = String.format(xpath, count, 5);

          Date date = Date.from(LocalDate.parse(driver.findElement(By.xpath(dateXpath)).getText(), dateFormat).atStartOfDay().toInstant(currentZoneOffset));
          float usd = Float.parseFloat(driver.findElement(By.xpath(usdXpath)).getText());
          float gbp = Float.parseFloat(driver.findElement(By.xpath(gbpXpath)).getText());
          float euro = Float.parseFloat(driver.findElement(By.xpath(euroXpath)).getText());
          float yen = Float.parseFloat(driver.findElement(By.xpath(yenXpath)).getText());
          currencyList.add(new Currency(UUID.randomUUID(), date, usd, gbp, euro, yen));
          System.out.println(String.format("%s,%f,%f,%f,%f",date, usd, gbp, euro, yen));
          ++count;
          if (currencyList.size() > 1000) {
            insertRecords(currencyList);
          }
        }
      }
      catch (Exception e) {

      }
    }
    for (File f : dataDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().contains("RBIREFRATE");
      }
    })) {
      DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
              .parseCaseInsensitive()
              .parseLenient()
              .appendPattern("dd-MMM-yyyy")
              .toFormatter(Locale.ENGLISH);
      ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
      System.out.println(f.getAbsolutePath());
      driver.get("file://" + f.getAbsolutePath());
      String xpath = "/html/body/div/table/tbody/tr[%d]/td[%d]";
      int count = 2;
      try {
        while (true) {

          String dateXpath = String.format(xpath, count, 1);
          String usdXpath = String.format(xpath, count, 2);
          String gbpXpath = String.format(xpath, count, 4);
          String euroXpath = String.format(xpath, count, 3);
          String yenXpath = String.format(xpath, count, 5);

          Date date = Date.from(LocalDate.parse(driver.findElement(By.xpath(dateXpath)).getText(), dateFormat).atStartOfDay().toInstant(currentZoneOffset));
          float usd = Float.parseFloat(driver.findElement(By.xpath(usdXpath)).getText());
          float gbp = Float.parseFloat(driver.findElement(By.xpath(gbpXpath)).getText());
          float euro = Float.parseFloat(driver.findElement(By.xpath(euroXpath)).getText());
          float yen = Float.parseFloat(driver.findElement(By.xpath(yenXpath)).getText());
          currencyList.add(new Currency(UUID.randomUUID(), date, usd, gbp, euro, yen));
          System.out.println(String.format("%s,%f,%f,%f,%f",date, usd, gbp, euro, yen));
          ++count;
          if (currencyList.size() > 1000) {
            insertRecords(currencyList);
          }
        }
      }
      catch (Exception e) {

      }
    }
    if (currencyList.size() > 0) {
      insertRecords(currencyList);
    }
  }
  private void insertRecords(List<Currency> currencyList) {

    MongoCollection<Currency> collection = getCurrencyCollection();
    try {

      collection.insertMany(currencyList);
    }
    catch (Exception e) {
      currencyList.forEach( ce -> {
        try {

          collection.insertOne(ce);
        }
        catch (Exception e1) {

          System.out.println("Insert Currency failed.." + ce);
        }
      });
    }
    currencyList.clear();
  }
}