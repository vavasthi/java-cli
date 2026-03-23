package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerateDayTradeExcel extends Base {
    MongoCollection<Document> longStraddleCollection = getMongoClient().getDatabase("vipanan").getCollection("longStraddle");
    MongoCollection<Document> longStraddlePAndLCollection = getMongoClient().getDatabase("vipanan").getCollection("longStraddlePAndL");
    public static void main(String[] args) {
        GenerateDayTradeExcel generateDayTradeExcel = new GenerateDayTradeExcel();
        generateDayTradeExcel.run();
    }
    private void run() {

        Random random = new Random();
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try (Workbook workbook = new XSSFWorkbook()) {
            longStraddleCollection.find().cursor().forEachRemaining(row -> {
               Sheet sheet = workbook.createSheet(String.format("%.0f-%d", row.getDouble("strike"), random.nextInt(100)));
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Timestamp");
                header.createCell(1).setCellValue("P&L");
                AtomicInteger i = new AtomicInteger(1);
               longStraddlePAndLCollection.find(Filters.eq("lsId", row.get("_id"))).sort(Sorts.ascending("timestamp")).cursor().forEachRemaining(profit -> {
                   Row data = sheet.createRow(i.getAndIncrement());
                   data.createCell(0).setCellValue(sdf.format(profit.getDate("timestamp")));
                   data.createCell(1).setCellValue(profit.getDouble("pAndL"));;
               });
            });
            try (FileOutputStream out = new FileOutputStream("dump.xlsx")) {
                workbook.write(out);
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
