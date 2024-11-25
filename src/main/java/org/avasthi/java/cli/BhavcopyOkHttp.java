package org.avasthi.java.cli;

import kotlin.Pair;
import okhttp3.*;
import org.apache.pdfbox.io.IOUtils;

import javax.swing.*;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

public class BhavcopyOkHttp {

    public static void main(String[] args) throws IOException, InterruptedException {

        SimpleDateFormat ddmmyyFormat = new SimpleDateFormat("dd-MMM-YYYY");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.MONTH, 9);
        calendar.set(Calendar.YEAR, 2022);
        OkHttpClient client = new OkHttpClient();
        BhavcopyOkHttp bcoh = new BhavcopyOkHttp();
        Headers headers = bcoh.allReports(client);
        System.out.println(headers.toMultimap());
        while (calendar.before(Calendar.getInstance())) {

            try {

                bcoh.downloadBhavCopy(client, headers, calendar, ddmmyyFormat);
                Thread.sleep(2000);
            } catch (Exception e) {
                Thread.sleep(5000);
                System.out.println("Retrying");
                try {

                    bcoh.downloadBhavCopy(client, headers, calendar, ddmmyyFormat);
                } catch (Exception e1) {

                    Thread.sleep(10000);
                    System.out.println("Retrying again");
                    try {

                        bcoh.downloadBhavCopy(client, headers, calendar, ddmmyyFormat);
                    } catch (Exception e2) {

                        Thread.sleep(10000);
                        System.out.println("Retrying again again");
                        try {
                            bcoh.downloadBhavCopy(client, headers, calendar, ddmmyyFormat);
                        } catch (Exception e3) {

                            System.out.println("Download FAILED .. " + calendar.getTime());
                        }
                    }
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public Headers allReports(OkHttpClient client) {
        String url = "https://www.nseindia.com/all-reports";
        Request request = new Request.Builder()
                .url(url)
                .headers(defaultHeaders(new Headers.Builder()).build())
                .get()
                .build();
        Headers.Builder nextRequestHeaderBuilder = new Headers.Builder();
        try (Response response = client.newCall(request).execute()) {
            Headers headers = response.headers();
            for (Iterator<Pair<String, String>> it = headers.iterator(); it.hasNext(); ) {
                Pair<String, String> kv = it.next();
                if (kv.getFirst().equalsIgnoreCase("set-cookie")) {
                    nextRequestHeaderBuilder.add("Cookie", kv.getSecond());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return nextRequestHeaderBuilder.build();
    }
    private Headers.Builder defaultHeaders(Headers.Builder builder) {

        builder.add("Accept-Encoding", "gzip, deflate, br, zstd");
        builder.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
        return builder;
    }
    private Headers.Builder allHeaders(Headers.Builder builder,
                                       Headers headers) {
        builder = defaultHeaders(builder);
        for (Iterator<Pair<String, String>> it = headers.iterator(); it.hasNext(); ) {
            Pair<String, String> kv = it.next();
            builder.add(kv.getFirst(), kv.getSecond());
        }
        return builder;
    }
    private void downloadBhavCopy(OkHttpClient client,
                                  Headers headers,
                                  Calendar calendar,
                                  SimpleDateFormat ddmmyyFormat) throws IOException, InterruptedException {
        Calendar cutoff = Calendar.getInstance();
        cutoff.set(2024, 6, 6, 0, 0, 0);
        if (calendar.before(cutoff)) {

            String values = "[{\"name\":\"CM - Bhavcopy(csv)\",\"type\":\"archives\",\"category\":\"capital-market\",\"section\":\"equities\"}]";
            String bhavCopyURLTemplate = URLEncoder.encode(values, "UTF-8");
            System.out.println(URLDecoder.decode(bhavCopyURLTemplate, "UTF-8"));
            String bhavCopyURL = "https://www.nseindia.com/api/reports?archives=" + bhavCopyURLTemplate + "&date=" + ddmmyyFormat.format(calendar.getTime()) + "&type=equities&mode=single";
            downloadBhavCopy(client, bhavCopyURL, headers, calendar, ddmmyyFormat);
        }
        else {

            String values = "[{\"name\":\"CM-UDiFF Common Bhavcopy Final (zip)\",\"type\":\"daily-reports\",\"category\":\"capital-market\",\"section\":\"equities\"}]";
            String bhavCopyURLTemplate = URLEncoder.encode(values, "UTF-8");
            System.out.println(URLDecoder.decode(bhavCopyURLTemplate, "UTF-8"));
            String bhavCopyURL = "https://www.nseindia.com/api/reports?archives=" + bhavCopyURLTemplate + "&date=" + ddmmyyFormat.format(calendar.getTime()) + "&type=equities&mode=single";
            downloadBhavCopy(client, bhavCopyURL, headers, calendar, ddmmyyFormat);
        }
    }
    private void downloadBhavCopy(OkHttpClient client,
                                  String bhavCopyURL,
                                  Headers headers,
                                  Calendar calendar,
                                  SimpleDateFormat ddmmyyFormat) throws IOException, InterruptedException {

        Request request = new Request.Builder()
                .url(bhavCopyURL)
                .headers(allHeaders(new Headers.Builder(), headers).build())
                .get()
                .build();
        int retries = 3;
        boolean success = false;
        while (retries > 0 && !success) {

            try (Response response = client.newCall(request).execute()) {
                String cd = response.headers().get("content-disposition");
                String outfile = cd.split("=")[1].replace("\"", "");
                String homeDir = System.getenv("HOME");
                File outfullPath = new File(new File(homeDir, "Downloads"), outfile);
                byte[] buffer = new byte[81920];
                FileOutputStream fileWriter = new FileOutputStream(outfullPath);
                System.out.println("Writing to " + outfullPath.getAbsolutePath());
                IOUtils.copy(response.body().byteStream(), fileWriter);
                fileWriter.close();
                success = true;
                retries = 0;
                return;
            }
            catch (SocketTimeoutException ex) {
                --retries;
                Thread.sleep(5000);
            }
        }
    }
}
