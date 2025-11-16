package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.pdfbox.io.IOUtils;
import org.avasthi.java.cli.pojos.StockMaster;
import org.brotli.dec.BrotliInputStream;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class DownloadNSEQuarterlyReportTable extends Base {

    public static void main(String[] args) throws IOException, InterruptedException {


        DownloadNSEQuarterlyReportTable bcoh = new DownloadNSEQuarterlyReportTable();
        bcoh.download();

    }

    private void download() throws InterruptedException, FileNotFoundException {
        MongoDatabase db = getMongoClient().getDatabase(database);
        MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
        for (StockMaster sm : collection.find(Filters.or(Filters.exists("sensex"), Filters.exists("nifty50")))) {

            download(sm.getSymbol());
        }

    }
    private void download(String symbol) throws FileNotFoundException {

        String url = "https://www.nseindia.com/api/corporates-financial-results?index=equities&from_date=01-01-1990&to_date=15-10-2025&symbol=%s&period=Quarterly";
        WebDriver driver = getChromeDriver(false);
        driver.get("https://www.nseindia.com/companies-listing/corporate-filings-financial-results");
        WebElement input = new WebDriverWait(driver, Duration.ofMinutes(10)).until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[12]/div[1]/div/section/div/div/div[2]/div/div[2]/div[1]/div[1]/div[1]/div/span/input")));

        input.click();
        input.sendKeys(symbol);
        input.sendKeys(Keys.DOWN);
        input.sendKeys(Keys.RETURN);
        driver.get(String.format(url, symbol));
        String html = driver.getPageSource();
        File file = new File(System.getenv("HOME") + "/Downloads", String.format("%s.json", symbol));
        PrintWriter pw = new PrintWriter(file);
        for (String s : Jsoup.parse(html).body().getElementsByTag("pre").eachText()) {
            pw.println(s);
        }
        pw.close();
        driver.close();
    }
}
