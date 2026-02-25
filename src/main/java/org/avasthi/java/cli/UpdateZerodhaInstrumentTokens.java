package org.avasthi.java.cli;

import com.opencsv.CSVParser;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.csv.CSVException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.io.IOUtils;
import org.avasthi.java.cli.pojos.ExchangeSegment;
import org.avasthi.java.cli.pojos.ZerodhaInstruments;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UpdateZerodhaInstrumentTokens {

    public static void main(String[] args) throws IOException, InterruptedException {
        UpdateZerodhaInstrumentTokens uzit = new UpdateZerodhaInstrumentTokens();
        uzit.download();
    }

    public void download() {
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        String url = "https://api.kite.trade/instruments";
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("X-Kite-Version", "3");
        Request request = new Request.Builder()
                .url(url)
                .headers(allHeaders(new Headers.Builder(), Headers.of(extraHeaders)).build())
                .get()
                .build();
        CSVFormat format = CSVFormat
                .DEFAULT
                .builder()
                .setSkipHeaderRecord(true)
                .setHeader("instrument_token", "exchange_token", "symbol", "name", "last_price", "expiry", "strike", "tick_size", "lot_size", "instrument_type", "segment", "exchange").build();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        try (Response response = client.newCall(request).execute()) {
            InputStream is = response.body().byteStream();
            org.apache.commons.csv.CSVParser parser = org.apache.commons.csv.CSVParser.parse(is, Charset.defaultCharset(), format);
            try {

                parser.stream().forEach(record -> {
                    try {
                        Date expiry = Date.from(LocalDate.parse(record.get("expiry"), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(ZoneId.systemDefault()).toInstant());
                        ZerodhaInstruments zi = new ZerodhaInstruments(record.get("instrument_token"),
                                record.get("exchange_token"),
                                record.get("symbol"),
                                record.get("name"),
                                expiry,
                                Long.parseLong(record.get("strike")),
                                Float.parseFloat(record.get("tick_size")),
                                Long.parseLong(record.get("lot_size")),
                                record.get("instrument_type"),
                                ExchangeSegment.create(record.get("exchange"), record.get("segment")));
                        System.out.println(zi);
                    } catch (Exception e) {
                        System.out.println(String.format("ERROR %s", record));
                    }
                });
            } catch (UncheckedIOException cex) {
                System.out.println(cex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Headers.Builder defaultHeaders(Headers.Builder builder) {

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
}
