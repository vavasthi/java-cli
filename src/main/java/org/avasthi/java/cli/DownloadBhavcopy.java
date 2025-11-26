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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class DownloadBhavcopy extends Base{
    public static void main(String[] args) throws IOException, InterruptedException {

        DownloadBhavcopy dbc = new DownloadBhavcopy();
        Calendar today = Calendar.getInstance();
        dbc.download();
    }
    private void download() throws UnsupportedEncodingException, InterruptedException {

      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.DAY_OF_MONTH, 3);
      calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
      calendar.set(Calendar.YEAR, 1994);
      SimpleDateFormat ddmmyyFormat = new SimpleDateFormat("dd-MMM-YYYY");
      File downloadDir = new File("/data/datasets/Bhavcopy", "current");
      Map<String, Object> chromePrefs = new HashMap<String, Object>();
      // Set the default download directory
      chromePrefs.put("download.default_directory", downloadDir.getAbsolutePath());
      // Disable the download prompt to automatically save files
      chromePrefs.put("download.prompt_for_download", false);
      // Disable the built-in PDF viewer, forcing external handling (download)
      chromePrefs.put("plugins.always_open_pdf_externally", true);
      // Optional: disable popup blocking
      chromePrefs.put("profile.default_content_settings.popups", 0);
      WebDriver driver = getWebDriver(false, chromePrefs);

      Calendar endDate = Calendar.getInstance();
      endDate.set(Calendar.DAY_OF_MONTH, 8);
      endDate.set(Calendar.MONTH, Calendar.JULY);
      endDate.set(Calendar.YEAR, 2024);
      while(calendar.before(endDate)) {
        System.out.printf("Downloading for %s\n", calendar.getTime());
        downloadForADay(driver, calendar, ddmmyyFormat);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
      }
    }
    private void downloadForADay(WebDriver driver,
                                 Calendar calendar,
                                 SimpleDateFormat ddmmyyFormat) throws InterruptedException, UnsupportedEncodingException {

      String url = String.format("https://www.nseindia.com/api/reports?archives=[{\"name\":\"CM - Bhavcopy(csv)\",\"type\":\"archives\",\"category\":\"capital-market\",\"section\":\"equities\"},{\"name\":\"CM - Common Bhavcopy (csv)\",\"type\":\"archives\",\"category\":\"capital-market\",\"section\":\"equities\"}]&date=%s&type=Archives", ddmmyyFormat.format(calendar.getTime()));
      driver.get(url);
      Thread.sleep(250);
    }
}