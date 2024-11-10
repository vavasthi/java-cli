package org.avasthi.java.cli;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DownloadBhavcopyOnlyKeyStrokes {
    public static void main(String[] args) throws IOException, InterruptedException {

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setAcceptInsecureCerts(true);
//        chromeOptions.setExperimentalOption("networkConnectionEnabled",true);
        chromeOptions.addArguments("--no-sandbox");
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.manage().window().setSize(new Dimension(1500, 1500));
        driver.get("https://www.nseindia.com/all-reports");
        driver.findElement(By.id("Archives_rpt")).click();
        new Actions(driver).moveToElement(driver.findElement(By.id("cr_equity_archives_date"))).click().perform();
        driver.findElement(By.cssSelector(".gj-picker div:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".gj-picker div:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".fa-chevron-left")).click();
        driver.findElement(By.cssSelector("tr:nth-child(2) > td:nth-child(3) > div")).click();
        driver.findElement(By.cssSelector("tr:nth-child(1) > td:nth-child(1) > div")).click();
        driver.findElement(By.cssSelector("tr:nth-child(4) > .current-month:nth-child(2) > div")).click();
        driver.findElement(By.cssSelector("#cr_equity_archives .col-lg-4:nth-child(10) .pdf-download-link")).click();
    }
}