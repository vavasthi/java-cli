package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import org.avasthi.java.cli.pojos.StockMaster;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuarterlyFilingsIndexStocks extends Base {

    ExecutorService executorService = Executors.newFixedThreadPool(5);
    public static void main(String[] args) throws IOException, InterruptedException {


        OkHttpClient client = new OkHttpClient();
        QuarterlyFilingsIndexStocks bcoh = new QuarterlyFilingsIndexStocks();
        Headers headers = bcoh.allReports(client);
        System.out.println(headers.toMultimap());
        try {

            //bcoh.downloadQuarterlyFilings();
            //bcoh.getListOfUrls();
            bcoh.downloadAllCsvs();
            //Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed " + e.toString());
        }
    }

    List<String> getListOfUrls() throws IOException {
        MongoDatabase db = getMongoClient().getDatabase(database);
        MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
        List<String> urls = new ArrayList<>();
        for (StockMaster doc : collection.find(Filters.or(Filters.eq("nifty50", true), Filters.eq("sensex", true)))) {

            getUrlsForCompany(doc.getStockCode(), urls);
        }
        return urls;
    }
    List<String> getUrlsForCompany(String stockCode, List<String> urls) {

        for (int i = 30;i < 125;++i) {

            urls.add(String.format("https://www.bseindia.com/corporates/results.aspx?Code=%s&qtr=%d.00&RType=\n", stockCode, i));
        }
        return urls;
    }
    private void downloadAllCsvs() throws IOException {
        getListOfUrls().forEach(url -> {
            executorService.submit(new Runnable() {
                @Override
                public void run() {

                    System.out.println(url);
                    downloadSingleQuarterFile(url);
                }
            });
        });
    }
    private void downloadSingleQuarterFile(String quarterUrl) {
        WebDriver driver = getWebDriver();
        try {
            driver.get(quarterUrl);
            try {

                WebElement downloadButton = driver.findElement(By.xpath("//*[@id=\"ContentPlaceHolder1_lnkDownload\"]"));
                downloadButton.click();
                Thread.sleep(5000);
            }
            catch (NoSuchElementException nse) {

                WebElement stockCode = driver.findElement(By.xpath("//*[@id=\"ContentPlaceHolder1_lblsrcipcode\"]"));
                WebElement startReportingPeriod = driver.findElement(By.xpath("//*[@id=\"ContentPlaceHolder1_gv_Profit\"]/tbody/tr[2]/td[3]"));
                WebElement endReportingPeriod = driver.findElement(By.xpath("//*[@id=\"ContentPlaceHolder1_gv_Profit\"]/tbody/tr[3]/td[3]"));
                File htmlFile = new File(System.getenv("HOME") + "/Downloads", String.format("%s-%s-%s.html", stockCode.getText(), startReportingPeriod.getText().replaceAll("/",""), endReportingPeriod.getText().replaceAll("/", "")));
                Files.writeString(htmlFile.toPath(), driver.getPageSource());
            }
            driver.quit();
        } catch (Exception e) {
             System.out.println("FAILED for URL " + quarterUrl);
             e.printStackTrace();
             driver.quit();
        }
    }

}
