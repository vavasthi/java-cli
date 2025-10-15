package org.avasthi.java.cli;

import okhttp3.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NSEQuarterlyFilings extends Base {

    ExecutorService executorService = Executors.newFixedThreadPool(5);
    public static void main(String[] args) throws IOException, InterruptedException {

        NSEQuarterlyFilings qf = new NSEQuarterlyFilings();
        WebDriver driver = qf.getWebDriver();
        driver.get("https://www.nseindia.com/companies-listing/corporate-filings-financial-results");
        Thread.sleep(5000);
        driver.get("https://www.nseindia.com/api/corporates-financial-results?index=equities&from_date=01-01-1900&to_date=29-09-2025&symbol=RELIANCE&issuer=Reliance%20Industries%20Limited&period=Quarterly");
        System.out.println(driver.getPageSource());
            try {

            //bcoh.downloadQuarterlyFilings();
            //bcoh.getListOfUrls();
            //Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed " + e.toString());
        }
    }

    List<String> getListOfUrls() throws IOException {
        List<String> urls = new ArrayList<>();
        File csvData = new File(new File(System.getenv("HOME"), "Downloads"), "List_of_companies.csv");
        BufferedReader reader = new BufferedReader(new FileReader(csvData));
        CSVParser parser = CSVParser.parse(reader, CSVFormat.RFC4180);
        for (CSVRecord csvRecord : parser) {
            try {

                int stockCode = Integer.parseInt(csvRecord.get(1));
                for (int i = 30;i < 125;++i) {

                    urls.add(String.format("https://www.bseindia.com/corporates/results.aspx?Code=%d&qtr=%d.00&RType=\n", stockCode, i));
                }
            }
            catch (Exception e) {

            }
        }
        return urls;
    }
    private void downloadAllCsvs() throws IOException {

        WebDriver driver = getWebDriver();
        driver.get("https://www.nseindia.com/api/corporates-financial-results?index=equities&from_date=01-01-1989&to_date=29-09-2025&symbol=RELIANCE&issuer=Reliance%20Industries%20Limited&period=Quarterly");
        //System.out.println(driver.getPageSource());
        return;
/*        getListOfUrls().forEach(url -> {
            executorService.submit(new Runnable() {
                @Override
                public void run() {

                    System.out.println(url);
                    downloadSingleQuarterFile(url);
                }
            });
        });*/
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
