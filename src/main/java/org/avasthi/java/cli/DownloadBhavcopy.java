package org.avasthi.java.cli;

import com.google.gson.*;
import org.avasthi.java.cli.pojos.IndexPrice;
import org.avasthi.java.cli.pojos.IndexSymbols;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class DownloadBhavcopy extends Base{
  public static void main(String[] args) throws IOException, InterruptedException {

    DownloadBhavcopy dbc = new DownloadBhavcopy();
    Calendar today = Calendar.getInstance();
    dbc.downloadIndexPrices();
  }
  private void download() throws UnsupportedEncodingException, InterruptedException {

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 8);
    calendar.set(Calendar.MONTH, Calendar.JULY);
    calendar.set(Calendar.YEAR, 1990);
    // For F & O
    calendar.set(Calendar.DAY_OF_MONTH, 7);
    calendar.set(Calendar.MONTH, Calendar.JULY);
    calendar.set(Calendar.YEAR, 2000);
    SimpleDateFormat ddmmyyFormat = new SimpleDateFormat("dd-MMM-YYYY");
    File downloadDir = new File("/data/datasets/Bhavcopy", "FnO");
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
    while(calendar.before(endDate)) {
      System.out.printf("Downloading for %s\n", calendar.getTime());
      downloadIndexPrices(driver, calendar, ddmmyyFormat);
      calendar.add(Calendar.DAY_OF_MONTH, 1);
    }
  }
  private void downloadForADay(WebDriver driver,
                               Calendar calendar,
                               SimpleDateFormat ddmmyyFormat) throws InterruptedException, UnsupportedEncodingException {

    String url = String.format("https://www.nseindia.com/api/reports?archives=[{\"name\":\"CM-UDiFF Common Bhavcopy Final (zip)\",\"type\":\"daily-reports\",\"category\":\"capital-market\",\"section\":\"equities\"}]&date=%S&type=Archives", ddmmyyFormat.format(calendar.getTime()));

    driver.get(url);
    Thread.sleep(250);
  }
  private void downloadFAndO(WebDriver driver,
                             Calendar calendar,
                             SimpleDateFormat ddmmyyFormat) throws InterruptedException, UnsupportedEncodingException {

    Calendar newFormatDate = Calendar.getInstance();
    newFormatDate.set(Calendar.DAY_OF_MONTH, 7);
    newFormatDate.set(Calendar.MONTH, Calendar.JULY);
    newFormatDate.set(Calendar.YEAR, 2024);

    String archives = "%5B%7B%22name%22%3A%22F%26O%20-%20Bhavcopy(csv)%22%2C%22type%22%3A%22archives%22%2C%22category%22%3A%22derivatives%22%2C%22section%22%3A%22equity%22%7D%5D&date="+ddmmyyFormat.format(calendar.getTime()) +"&type=equity&mode=single";
    if (calendar.after(newFormatDate)) {
      archives = "%5B%7B%22name%22%3A%22F%26O%20-%20UDiFF%20Common%20Bhavcopy%20Final%20(zip)%22%2C%22type%22%3A%22archives%22%2C%22category%22%3A%22derivatives%22%2C%22section%22%3A%22equity%22%7D%5D&date=" + ddmmyyFormat.format(calendar.getTime()) + "&type=equity&mode=single";
    }
    String url = String.format("https://www.nseindia.com/api/reports?archives=%s", archives);
    System.out.println(url);
    driver.get(url);
    Thread.sleep(250);
  }
  private void downloadIndexPrices() {

    File downloadDir = new File("/data/datasets/Bhavcopy", "FnO");
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
    Calendar now = Calendar.getInstance();
    SimpleDateFormat ddmmyyFormat = new SimpleDateFormat("dd-MMM-YYYY");
    int year = 1980;

    List<IndexPrice> indexPriceList = new ArrayList<>();
    while(getFirstDayOfYear(year).before(now)) {
      String startDay = String.format("01-01-%d", year);
      String endDay = String.format("31-12-%d", year);
      String url = "https://www.nseindia.com/api/historicalOR/indicesHistory?indexType=NIFTY%2050&from=" + startDay + "&to=" + endDay + "&csv=true";
      driver.get(url);
      System.out.println(url);
      Gson gson = new Gson();
      try {

        JsonArray array = gson.fromJson(driver.findElement(By.tagName("pre")).getText(), JsonObject.class).getAsJsonArray("data");
        for (JsonElement elem : array) {
          Map<String, JsonElement> map = elem.getAsJsonObject().asMap();
          String name = map.get("EOD_INDEX_NAME").getAsString();
          float open = map.get("EOD_OPEN_INDEX_VAL").getAsFloat();
          float high = map.get("EOD_HIGH_INDEX_VAL").getAsFloat();
          float close = map.get("EOD_CLOSE_INDEX_VAL").getAsFloat();
          float low = map.get("EOD_LOW_INDEX_VAL").getAsFloat();
          float turnover = 0;
          try {

            turnover = map.get("HIT_TURN_OVER").getAsFloat();
          }
          catch (Exception e) {

          }
          long tradedQuantity = 0;
          try {

            tradedQuantity = map.get("HIT_TRADED_QTY").getAsLong();
          }
          catch (Exception e) {

          }
          Date date = ddmmyyFormat.parse(map.get("EOD_TIMESTAMP").getAsString());
          IndexPrice ip = new IndexPrice(UUID.randomUUID(),
                  date,
                  name,
                  IndexSymbols.getFromFullname(name),
                  open,
                  high,
                  low,
                  close,
                  turnover,
                  tradedQuantity);
          indexPriceList.add(ip);
        }
      }
      catch(JsonSyntaxException e) {

        System.out.println(driver.findElement(By.tagName("pre")));
      }
      catch (org.openqa.selenium.NoSuchElementException nee) {
        --year;
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
      ++year;
    }
    if (indexPriceList.size() > 0) {
      getIndexPriceCollection().insertMany(indexPriceList);
    }
  }
  private void downloadIndexPrices(WebDriver driver,
                             Calendar calendar,
                             SimpleDateFormat ddmmyyFormat) throws InterruptedException, UnsupportedEncodingException {

    Calendar newFormatDate = Calendar.getInstance();
    newFormatDate.set(Calendar.DAY_OF_MONTH, 7);
    newFormatDate.set(Calendar.MONTH, Calendar.JULY);
    newFormatDate.set(Calendar.YEAR, 2024);

    String archives = "%5B%7B%22name%22%3A%22Daily%20Snapshot%22%2C%22type%22%3A%22archives%22%2C%22category%22%3A%22capital-market%22%2C%22section%22%3A%22indices%22%7D%5D&date="+ddmmyyFormat.format(calendar.getTime()) +"&type=indices&mode=single";
    String url = String.format("https://www.nseindia.com/api/reports?archives=%s", archives);
    System.out.println(url);
    driver.get(url);
    Thread.sleep(250);
  }
  private Calendar getFirstDayOfYear(int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.MONTH, Calendar.JANUARY);
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar;
  }
}