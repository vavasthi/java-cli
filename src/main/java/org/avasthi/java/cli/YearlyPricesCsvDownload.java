package org.avasthi.java.cli;

import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.pdfbox.io.IOUtils;
import org.brotli.dec.BrotliInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

public class YearlyPricesCsvDownload {

    public static void main(String[] args) throws IOException, InterruptedException {


        SimpleDateFormat ddmmyyFormat = new SimpleDateFormat("dd-MM-YYYY");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.YEAR, 1994);
        OkHttpClient client = new OkHttpClient();
        YearlyPricesCsvDownload bcoh = new YearlyPricesCsvDownload();
        Headers headers = bcoh.allReports(client);
        System.out.println(headers.toMultimap());
        while (calendar.before(Calendar.getInstance())) {

            try {

                bcoh.downloadStockPriceCsv(client, "RELIANCE", headers, calendar);
            }
            catch (Exception e) {
                System.out.println("Failed for " + calendar.getTime().toString() + " " + e.toString());
            }
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
    private void downloadStockPriceCsv(OkHttpClient client,
                                       String symbol,
                                       Headers headers,
                                       Calendar calendar) throws IOException, InterruptedException {
        SimpleDateFormat ddmmyyFormat = new SimpleDateFormat("dd-MM-YYYY");
        String stockData = "https://www.nseindia.com/api/historical/securityArchives?dataType=priceVolumeDeliverable&series=ALL&csv=true";
        String fromDate = ddmmyyFormat.format(calendar.getTime());
        calendar.add(Calendar.YEAR, 1);
        String toDate = ddmmyyFormat.format(calendar.getTime());
        String urlSuffix = "from=" + fromDate + "&to=" + toDate + "&symbol=" + symbol;
        String url = stockData + "&" + urlSuffix;
        downloadStockPriceCsv(client, url, headers);
    }
    private void downloadStockPriceCsv(OkHttpClient client,
                                       String url,
                                       Headers headers) throws IOException, InterruptedException {

        Request request = new Request.Builder()
                .url(url)
                .headers(allHeaders(new Headers.Builder(), headers).build())
                .get()
                .build();
        int retries = 3;
        boolean success = false;
        while (retries > 0 && !success) {

            try (Response response = client.newCall(request).execute()) {
                String cd = response.header("content-disposition");
                String ce = response.header("content-encoding");
                String outfile = cd.split("=")[1].replace("\"", "");
                String homeDir = System.getenv("HOME");
                if (homeDir == null) {
                    homeDir = System.getenv("HOMEPATH");
                }
                File outfullPath = new File(new File(homeDir, "Downloads"), outfile);
                byte[] buffer = new byte[81920];
                FileOutputStream fileWriter = new FileOutputStream(outfullPath);
                System.out.println("Writing to " + outfullPath.getAbsolutePath());
                InputStream is = response.body().byteStream();
                if (ce.equalsIgnoreCase("br")) {
                    is = new BrotliInputStream(is);
                }
                IOUtils.copy(is, fileWriter);
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
