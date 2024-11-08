package org.avasthi.java.cli;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.safari.SafariDriver;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class DownloadBhavcopy {
    public static void main(String[] args) throws IOException, InterruptedException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 17);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.YEAR, 2015);
        SimpleDateFormat ddmmyyFormat = new SimpleDateFormat("dd-MMM-YYYY");

        DownloadBhavcopy dbc = new DownloadBhavcopy();
        Calendar today = Calendar.getInstance();
        while (today.after(calendar)) {


            WebDriver driver = new ChromeDriver();
            driver.get("https://www.nseindia.com/all-reports");
            WebElement archives = driver.findElement(By.id("Archives_rpt"));
            archives.click();
            WebElement archiveDiv = driver.findElement(By.id("cr_equity_archives"));
            WebElement dateInput = archiveDiv.findElement(By.id("cr_equity_archives_date"));
            String dataGuid = dateInput.getAttribute("data-guid");
            WebElement calendarElement = dbc.getCalendarElement(driver, dataGuid);
            boolean bhavCopyChecked = false;
            System.out.println(String.format("Downloading for calendar %s...", ddmmyyFormat.format(calendar.getTime())));
            SimpleDateFormat yymmddFormat = new SimpleDateFormat("YYYY-M-d");
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if (calendarElement != null) {
                dbc.setAttribute(driver, calendarElement, "selectedday", yymmddFormat.format(calendar.getTime()));
                dbc.setAttribute(driver, calendarElement, "month", String.valueOf(month));
                dbc.setAttribute(driver, calendarElement, "year", String.valueOf(year));
                dbc.setAttribute(driver, calendarElement, "day", String.valueOf(day));
            }
            System.out.println(calendarElement.getAttribute("selectedday") + calendarElement.getAttribute("day"));
            new Actions(driver).moveToElement(dateInput).click().perform();
            new Actions(driver).sendKeys(Keys.ENTER).perform();
            archiveDiv.findElement(By.cssSelector(".col-lg-4:nth-child(10) .chk_container")).click();
            driver.findElement(By.linkText("Multiple file Download")).click();
//            dbc.downloadForADay(driver, calendarElement, calendar, ddmmyyFormat);
            Thread.sleep(1000);
            calendar.add(Calendar.DATE, 1);
            driver.quit();
        }
    }
    private void downloadForADay(WebDriver driver,
                                 WebElement calendarElement,
                                 Calendar calendar,
                                 SimpleDateFormat ddmmyyFormat) throws InterruptedException {

        getDayElement(driver, calendarElement, calendar.get(Calendar.DAY_OF_MONTH));
        Thread.sleep(10000);
        String bhavCopyURLTemplate = "archives=%5B%7B%22name%22%3A%22CM%20-%20Bhavcopy(csv)%22%2C%22type%22%3A%22archives%22%2C%22category%22%3A%22capital-market%22%2C%22section%22%3A%22equities%22%7D%5D&type=Archives";
        String bhavCopyURL = "https://www.nseindia.com/api/reports?" + bhavCopyURLTemplate + "&date=" + ddmmyyFormat.format(calendar.getTime());
        System.out.println(bhavCopyURL);
        driver.get(bhavCopyURL);
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
}