package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.io.IOUtils;
import org.avasthi.java.cli.pojos.Bhav;
import org.avasthi.java.cli.pojos.StockMaster;
import org.brotli.dec.BrotliInputStream;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class QuarterlyFilings extends Base {

    public static void main(String[] args) throws IOException, InterruptedException {


        OkHttpClient client = new OkHttpClient();
        QuarterlyFilings bcoh = new QuarterlyFilings();
        Headers headers = bcoh.allReports(client);
        System.out.println(headers.toMultimap());
        try {

            bcoh.downloadQuarterlyFilings();
            //Thread.sleep(30000);
        } catch (Exception e) {
            System.out.println("Failed " + e.toString());
        }
    }

    private void downloadQuarterlyFilings() throws IOException, InterruptedException {
        WebDriver driver = getWebDriver();
        MongoCollection<StockMaster> stockMasterCollection
                = mongoClient.getDatabase(database).getCollection(this.stockMasterCollection, StockMaster.class);
        for (StockMaster sm : stockMasterCollection.find()) {
            String url = "https://www.nseindia.com/companies-listing/corporate-filings-financial-results";
            driver.get(url);
            Thread.sleep(5000);
            WebElement companyNameInput = driver.findElement(By.xpath("//*[@id=\"financials_equities_companyName\"]"));
            companyNameInput.sendKeys(sm.symbol());
            companyNameInput.sendKeys(Keys.ARROW_DOWN);
            companyNameInput.sendKeys(Keys.ENTER);
            String source = driver.getPageSource();
            System.out.println(source);
        }
        driver.quit();

    }

    private void downloadQuarterlyFilings(OkHttpClient client,
                                          String url,
                                          Headers headers) throws IOException, InterruptedException {

        Request request = new Request.Builder()
                .url(url)
                .headers(allHeaders(new Headers.Builder(), headers).build())
                .get()
                .build();
        int retries = 3;
        boolean success = false;
        while (retries > 0 && !success) {

            try (Response response = client.newCall(request).execute()) {
                String cd = response.header("content-disposition");
                String ce = response.header("content-encoding");
                String outfile = cd.split("=")[1].replace("\"", "");
                String homeDir = System.getenv("HOME");
                if (homeDir == null) {
                    homeDir = System.getenv("HOMEPATH");
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InputStream is = response.body().byteStream();
                if (ce.equalsIgnoreCase("br")) {
                    is = new BrotliInputStream(is);
                }
                IOUtils.copy(is, byteArrayOutputStream);
                String csv = new String(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.close();
                CSVFormat format = CSVFormat
                        .DEFAULT
                        .builder()
                        .setSkipHeaderRecord(true)
                        .setHeader("companyName", "audited", "cumulative", "consolidated", "indAs", "period", "periodEnded", "relativeTo", "xbrl", "exchangeReceivedTime", "exchangeDisseminationTime", "timeTaken").build();
                CSVParser parser = CSVParser.parse(csv, format);
                for (CSVRecord csvRecord : parser) {
                    parseCsvAndDownloadReports(client, headers, csvRecord);
                }
            }
        }
    }

    private void parseCsvAndDownloadReports(OkHttpClient client, Headers headers, CSVRecord csvRecord) {

    }
}
