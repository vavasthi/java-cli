package org.avasthi.java.cli;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY");

        DownloadBhavcopy dbc = new DownloadBhavcopy();
        Calendar today = Calendar.getInstance();
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.nseindia.com/all-reports");
        WebElement archives = driver.findElement(By.id("Archives_rpt"));
        archives.click();
        archives.sendKeys(Keys.TAB);
        archives.sendKeys(Keys.TAB);
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        WebElement archiveDiv = driver.findElement(By.id("cr_equity_archives"));
        WebElement dateInput = archiveDiv.findElement(By.id("cr_equity_archives_date"));
        String js = "arguments[0].setAttribute('value','"+ dateFormat.format(new Date())+"')";
        ((JavascriptExecutor)driver).executeScript(js, dateInput);
        WebElement button = driver.findElement(By.id("cr_equity_archives")).findElement(By.tagName("button"));
//        button.findElement(By.className("fa")).click();
        while (today.after(calendar)) {

            Date date = calendar.getTime();
            String strDate = dateFormat.format(date);
            dbc.downloadForADay(driver, strDate);
            Thread.sleep(1000);
            calendar.add(Calendar.DATE, 1);
        }
        driver.quit();
    }
    private void downloadForADay(WebDriver driver,
                                 String date)  {

        System.out.println(String.format("Downloading for date %s...", date));
//        driver.findElement(By.id("cr_equity_archives_date"))
        String bhavCopyURLTemplate = "archives=%5B%7B%22name%22%3A%22CM%20-%20Bhavcopy(csv)%22%2C%22type%22%3A%22archives%22%2C%22category%22%3A%22capital-market%22%2C%22section%22%3A%22equities%22%7D%5D&type=Archives";
        String bhavCopyURL = "https://www.nseindia.com/api/reports?" + bhavCopyURLTemplate + "&date=" + date;
        System.out.println(bhavCopyURL);
        driver.get(bhavCopyURL);
    }
}