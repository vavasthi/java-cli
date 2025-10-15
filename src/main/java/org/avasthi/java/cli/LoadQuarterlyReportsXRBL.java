package org.avasthi.java.cli;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.QuarterlyReportTable;
import org.avasthi.java.cli.pojos.StockMaster;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.text.ParseException;
import java.time.Duration;
import java.util.Map;

public class LoadQuarterlyReportsXRBL extends Base {

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {


        LoadQuarterlyReportsXRBL bcoh = new LoadQuarterlyReportsXRBL();
        bcoh.download();

    }

    private void download() throws InterruptedException, FileNotFoundException, ParseException {
        File dir = new File("/data/datasets/qrjson");
        PrintWriter pw = new PrintWriter(new File("/tmp/keys.txt"));
        for (File f :dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith("json");
            }
        })) {
            FileReader fr = new FileReader(f);
            Gson gson = new Gson();
            JsonArray array = gson.fromJson(fr, JsonArray.class);
            for (JsonElement e : array) {
                Map<String, JsonElement> map = e.getAsJsonObject().asMap();
                QuarterlyReportTable table = new QuarterlyReportTable(map.get("audited").getAsString(),
                        map.get("bank").getAsString(),
                        map.get("broadCastDate").isJsonNull() ? map.get("filingDate").getAsString() : map.get("broadCastDate").getAsString(),
                        map.get("companyName").getAsString(),
                        map.get("consolidated").getAsString(),
                        map.get("cumulative").getAsString(),
                        map.get("difference").getAsString(),
                        map.get("exchdisstime").isJsonNull() ? map.get("filingDate").getAsString() :  map.get("exchdisstime").getAsString(),
                        map.get("filingDate").getAsString(),
                        map.get("format").getAsString(),
                        map.get("fromDate").getAsString(),
                        map.get("indAs").getAsString(),
                        map.get("industry").getAsString(),
                        map.get("isin").getAsString(),
                        map.get("oldNewFlag").isJsonNull() ? "U" : map.get("oldNewFlag").getAsString(),
                        map.get("params").getAsString(),
                        map.get("period").getAsString(),
                        map.get("reInd").getAsString(),
                        map.get("relatingTo").getAsString(),
                        map.get("resultDescription").isJsonNull() ? null : map.get("resultDescription").getAsString(),
                        map.get("resultDetailedDataLink").isJsonNull() ? null : map.get("resultDetailedDataLink").getAsString(),
                        map.get("seqNumber").getAsString(),
                        map.get("symbol").getAsString(),
                        map.get("toDate").getAsString(),
                        map.get("xbrl").isJsonNull() ? null : map.get("xbrl").getAsString());
                System.out.println(table);
            }
        }
        pw.close();
    }
}
