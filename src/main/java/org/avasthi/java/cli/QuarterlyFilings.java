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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.file.Files;
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

            //bcoh.downloadQuarterlyFilings();
            bcoh.downloadAllCsvs();
            //Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed " + e.toString());
        }
    }

    private void downloadQuarterlyFilings() throws IOException, InterruptedException {
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(new File(System.getenv("HOME") + "/Downloads", "all-urls.txt")));
        MongoCollection<StockMaster> stockMasterCollection
                = mongoClient.getDatabase(database).getCollection(this.stockMasterCollection, StockMaster.class);
        for (StockMaster sm : stockMasterCollection.find()) {
            WebDriver driver = getWebDriver();
            try {

                driver.get("https://www.bseindia.com/corporates/Comp_Resultsnew.aspx");
                WebElement input = driver.findElement(By.xpath("//*[@id=\"ContentPlaceHolder1_SmartSearch_smartSearch\"]"));
                input.sendKeys(sm.isin());
                input.sendKeys(Keys.ARROW_DOWN);
                input.sendKeys(Keys.ENTER);
                WebElement submit = driver.findElement(By.xpath("//*[@id=\"ContentPlaceHolder1_btnSubmit\"]"));
                WebElement selectElement = driver.findElement(By.id("ContentPlaceHolder1_broadcastdd"));
                Select select = new Select(selectElement);
                select.selectByIndex(6);
                submit.click();
                Thread.sleep(2000);
                List<WebElement> rows = driver.findElements(By.xpath("//*[@id=\"ContentPlaceHolder1_gvData\"]/tbody/tr"));
                for (WebElement row : rows) {

                    List<WebElement> tds = row.findElements(By.className("tdcolumn"));
                    if (tds.size() >= 3) {

                        WebElement anchor = tds.get(3).findElement(By.tagName("a"));
                        String quarterUrl = anchor.getAttribute("href");
//                    downloadSingleQuarterFile(driver, quarterUrl);
                        fileWriter.write(quarterUrl);
                        fileWriter.newLine();
                    }
                }
                fileWriter.flush();
            }
            catch (Exception e) {
                System.out.println("FAILED for " + sm.toString());
                e.printStackTrace();
            }
            driver.quit();
        }
        fileWriter.close();
    }

    private void downloadAllCsvs() throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(new File(System.getenv("HOME") + "/Downloads", "all-urls.txt")));
        br.lines().forEach( url -> {
            downloadSingleQuarterFile(url);
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
