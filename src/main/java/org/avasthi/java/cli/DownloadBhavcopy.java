package org.avasthi.java.cli;

import com.google.gson.Gson;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class DownloadBhavcopy {
    public static void main(String[] args) throws IOException, InterruptedException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 19);
        calendar.set(Calendar.MONTH, 10);
        calendar.set(Calendar.YEAR, 2024);
        SimpleDateFormat ddmmyyFormat = new SimpleDateFormat("dd-MMM-YYYY");

        DownloadBhavcopy dbc = new DownloadBhavcopy();
        Calendar today = Calendar.getInstance();
        WebDriver driver = dbc.getWebDriver();
        driver.get("https://www.nseindia.com/all-reports");
        WebElement archives = driver.findElement(By.id("Archives_rpt"));
        archives.click();
        WebElement archiveDiv = driver.findElement(By.id("cr_equity_archives"));
        WebElement dateInput = archiveDiv.findElement(By.id("cr_equity_archives_date"));
        String dataGuid = dateInput.getAttribute("data-guid");
        WebElement calendarElement = dbc.getCalendarElement(driver, dataGuid);
        SimpleDateFormat yymmddFormat = new SimpleDateFormat("YYYY-M-d");
        System.out.println(String.format("Downloading for calendar %s...", yymmddFormat.format(calendar.getTime())));
        while (today.after(calendar)) {

            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (calendarElement != null) {
                dbc.setAttribute(driver, calendarElement, "selectedday", yymmddFormat.format(calendar.getTime()));
                dbc.setAttribute(driver, calendarElement, "month", String.valueOf(month));
                dbc.setAttribute(driver, calendarElement, "year", String.valueOf(year));
                dbc.setAttribute(driver, calendarElement, "day", String.valueOf(day));
            }
            new Actions(driver).moveToElement(dateInput).click().perform();
            new Actions(driver).sendKeys(Keys.RIGHT).perform();
            new Actions(driver).sendKeys(Keys.RETURN).perform();
//            archiveDiv.findElement(By.cssSelector(".col-lg-4:nth-child(10) .chk_container")).click();
//            driver.findElement(By.linkText("Multiple file Download")).click();
            dbc.downloadForADay(driver, calendar, ddmmyyFormat);
            calendar.add(Calendar.DATE, 1);
        }
        driver.quit();
    }
    private void downloadForADay(WebDriver driver,
                                 Calendar calendar,
                                 SimpleDateFormat ddmmyyFormat) throws InterruptedException, UnsupportedEncodingException {
/*        Gson gson = new Gson();
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("name", "CM - Bhavcopy(csv)");
        valueMap.put("type", "archives");
        valueMap.put("category", "capital-market");
        valueMap.put("section", "equities");
        List<Map<String, String>> values = new ArrayList<>();
        values.add(valueMap);
        System.out.println(gson.toJson(values));*/
        String values = "[{\"name\":\"CM - Bhavcopy(csv)\",\"type\":\"archives\",\"category\":\"capital-market\",\"section\":\"equities\"}]";
        String bhavCopyURLTemplate = URLEncoder.encode(values, "UTF-8");
        System.out.println(URLDecoder.decode(bhavCopyURLTemplate, "UTF-8"));
        String bhavCopyURL = "https://www.nseindia.com/api/reports?archives=" + bhavCopyURLTemplate + "&date=" + ddmmyyFormat.format(calendar.getTime()) + "&type=equities&mode=single";
        System.out.println(String.format("<a href=\"%s\">Click</a>", bhavCopyURL));
        WebElement downloadLink = driver.findElement(By.cssSelector("#cr_equity_archives .col-lg-4:nth-child(10) .pdf-download-link"));
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.elementToBeClickable(downloadLink));
        downloadLink.click();

//        driver.get(bhavCopyURL);
//        driver.navigate().back();
    }
    private void setAttribute(WebDriver driver, WebElement element, String attribute, String value) {

        String js = "arguments[0].setAttribute(arguments[1], arguments[2]);";
        ((JavascriptExecutor)driver).executeScript(js, element, attribute, value);
    }
    private WebElement getCalendarElement(WebDriver driver, String guid) {
        List<WebElement> webElementList = driver.findElements(By.className("gj-picker"));
        for (WebElement we:webElementList) {
            if (we.getAttribute("guid") != null && we.getAttribute("guid").equals(guid)) {
                return we;
            }
        }
        return null;
    }
    private void getDayElement(WebDriver driver, WebElement calendar, int day) {
        calendar.findElement(By.xpath(String.format("//td[@day='%d']", day))).findElement(By.xpath("//div[@role='button']")).click();
    }
    private WebDriver getWebDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setAcceptInsecureCerts(true);
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing_for_trusted_sources_enabled", false);
        prefs.put("safebrowsing.enabled",false);
        chromeOptions.setExperimentalOption("prefs", prefs);
        //chromeOptions.addArguments("--headless=chrome");
        return new ChromeDriver(chromeOptions);
    }
}