package org.avasthi.java.cli.political;

import org.avasthi.java.cli.Base;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadSIRRolls extends Base {
    ExecutorService executorService = Executors.newFixedThreadPool(50);
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        DownloadSIRRolls ber = new DownloadSIRRolls();
        ber.download();
    }

    private void download() throws InterruptedException {
        File downloadDir = new File(new File(System.getenv("HOME"), "Downloads"), "voterlist");
        downloadDir.mkdirs();
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
            final Integer constituency = 90;
            for (int j = 1; j < 450; ++j) {
                final Integer part = j;
                String downloadURl = String.format("https://ceo.karnataka.gov.in/uploads/BANGALORE%%20URBAN/AC%%20%d/A%03d%04d.pdf", constituency, constituency, part);
                download(driver, downloadURl);
            }
    }
    private void download12(int state) throws InterruptedException {
        String downloadFilepath = new File(new File(System.getenv("HOME"), "Downloads"), "voterlist").getAbsolutePath();
        Map<String, Object> chromePrefs = new HashMap<String, Object>();
        // Set the default download directory
        chromePrefs.put("download.default_directory", downloadFilepath);
        // Disable the download prompt to automatically save files
        chromePrefs.put("download.prompt_for_download", false);
        // Disable the built-in PDF viewer, forcing external handling (download)
        chromePrefs.put("plugins.always_open_pdf_externally", true);
        // Optional: disable popup blocking
        chromePrefs.put("profile.default_content_settings.popups", 0);
        WebDriver driver = getWebDriver(false, chromePrefs);
        for(int i = 1; i < 250; ++i)
        {
            final Integer constituency = i;
            for (int j = 1; j < 300; ++j) {

                final Integer part = j;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            download(driver, state, constituency, part);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }
    private void download(WebDriver driver, int state, int constituency, int part) throws InterruptedException {
        String url = String.format("https://www.eci.gov.in/sir/f2/S%d/data/OLDSIRROLL/S%d/%d/S%d_%d_%d.pdf", state, state, constituency, state, constituency, part);
        download(driver, url);
    }
    private void download(WebDriver driver, String url) throws InterruptedException {

        System.out.println(url);
        driver.get(url);
        Thread.sleep(1000);
    }
}
