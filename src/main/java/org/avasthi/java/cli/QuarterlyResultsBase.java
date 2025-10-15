package org.avasthi.java.cli;

import org.avasthi.java.cli.pojos.HeadDictionary;
import org.avasthi.java.cli.pojos.QuarterlyFilingsHeads;
import org.avasthi.java.cli.pojos.QuarterlyResults;
import org.avasthi.java.cli.pojos.StockMaster;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuarterlyResultsBase extends Base{

  protected void parseSingleQuarterlyHtml(File html, StockMaster sm, List<QuarterlyResults> documents) throws IOException {
    Document d = Jsoup.parse(html);
    Element table = d.body().getElementById("tbl_typeID");
    if (table != null) {

      Map<QuarterlyFilingsHeads, Object> values = new HashMap<>();
      Elements rows = table.getElementsByTag("tr");

      rows.forEach(row -> {
        Elements cells = row.getElementsByTag("td");
        String headStr = cells.get(0).ownText();
        Optional<QuarterlyFilingsHeads> headOptional = HeadDictionary.INSTANCE.get(headStr);
        if (headOptional.isPresent()) {


          QuarterlyFilingsHeads head = headOptional.get();
          String strValue = cells.get(1).ownText().replaceAll(",", "'").replaceAll(" ", "").replaceAll("'", "");
          try {

            if (strValue.equals("-")) {
              strValue = "";
            }
            values.put(head, convertValue(head, strValue, head.getClazz()));
          } catch (NumberFormatException | ParseException nfe) {
            nfe.printStackTrace();
            System.out.println("Wrong value format " + strValue + " " + head.getHead() + " " + html);
          }
        }
        else {
//          System.out.println(headStr);
        }
      });
      QuarterlyResults quarterlyResults =  new QuarterlyResults(sm.getIsin(), sm.getId(), sm.getSymbol(), sm.getStockCode(), values);
      documents.add(quarterlyResults);
    }
  }
  private <T>  T convertValue(QuarterlyFilingsHeads head, String strValue, Class<T> clazz) throws ParseException {
    SimpleDateFormat ddmmmyy = new SimpleDateFormat("dd-MMM-yy");
    return switch(head.getDataType()) {
      case STRING -> clazz.cast(strValue);
      case NUMBER -> clazz.cast(Float.parseFloat(strValue.equals("") ? "0.0" : strValue));
      case INTEGER -> clazz.cast(Integer.parseInt(strValue.equals("") ? "0" : strValue));
      case DATE -> clazz.cast(ddmmmyy.parse(strValue));
    };
  }
}
