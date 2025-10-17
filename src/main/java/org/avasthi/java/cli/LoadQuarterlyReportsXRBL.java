package org.avasthi.java.cli;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.avasthi.java.cli.pojos.QuarterlyFilingsHeads;
import org.avasthi.java.cli.pojos.QuarterlyReportTable;
import org.avasthi.java.cli.pojos.QuarterlyResults;
import org.avasthi.java.cli.pojos.StockMaster;
import org.bson.conversions.Bson;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoadQuarterlyReportsXRBL extends Base {


    public static void main(String[] args) throws IOException, InterruptedException, ParseException, ParserConfigurationException, SAXException, XPathExpressionException {


        LoadQuarterlyReportsXRBL bcoh = new LoadQuarterlyReportsXRBL();
        bcoh.download();
        //bcoh.parseXml(new File(System.getenv("HOME") + "/Downloads", "BANKING_117525_1359016_23012025122721.xml"));
    }

/*    private static Map<QuarterlyFilingsHeads, List<String>> createXPathMap() {
        Map<QuarterlyFilingsHeads, List<String>> rv = new HashMap<>();
        rv.put( QuarterlyFilingsHeads.PERIOD_BEGINNING, Arrays.asList("/xbrl/context[@id='OneD']/period/startDate"));
        rv.put(QuarterlyFilingsHeads.PERIOD_ENDING, Arrays.asList("/xbrl/context[@id='OneD']/period/endDate"));
        rv.put(QuarterlyFilingsHeads.SYMBOL, Arrays.asList("/xbrl/Symbol"));
        rv.put(QuarterlyFilingsHeads.STOCK_CODE, Arrays.asList("/xbrl/ScripCode"));
        rv.put(QuarterlyFilingsHeads.MSEI_SYMBOL, Arrays.asList("/xbrl/MSEISymbol"));
        rv.put(QuarterlyFilingsHeads.ISIN, Arrays.asList("/xbrl/ISIN"));
        rv.put(QuarterlyFilingsHeads.TYPE, Arrays.asList("/xbrl/WhetherResultsAreAuditedOrUnaudited[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.CASH_EARNING_PER_SHARE, Arrays.asList("/xbrl/BasicEarningsPerShareBeforeExtraordinaryItems[@contextRef='OneD']", "/xbrl/BasicEarningsLossPerShareFromContinuingOperations[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.BASIC_EPS, Arrays.asList("/xbrl/BasicEarningsPerShareAfterExtraordinaryItems[@contextRef='OneD']", "/xbrl/BasicEarningsLossPerShareFromContinuingAndDiscontinuedOperations[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.EPS, Arrays.asList("/xbrl/DilutedEarningsPerShareBeforeExtraordinaryItems[@contextRef='OneD']", "/xbrl/DilutedEarningsLossPerShareFromContinuingAndDiscontinuedOperations[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.DEPRECIATION, Arrays.asList("/xbrl/DepreciationDepletionAndAmortisationExpense[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.EQUITY, Arrays.asList("/xbrl/PaidUpValueOfEquityShareCapital[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.EXPENDITURE, Arrays.asList("/xbrl/ExpenditureExcludingProvisionsAndContingencies[@contextRef='OneD']", "/xbrl/Expenses[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.GROSS_PROFIT, Arrays.asList("/xbrl/OperatingProfitBeforeProvisionAndContingencies[@contextRef='OneD']", "/xbrl/ProfitBeforeExceptionalItemsAndTax[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.INTEREST, Arrays.asList("/xbrl/InterestEarned[@contextRef='OneD']"));
        rv.put(QuarterlyFilingsHeads.GROSS_PROFIT, Arrays.asList("/xbrl/OperatingProfitBeforeProvisionAndContingencies[@contextRef='OneD']", "/xbrl/ProfitBeforeExceptionalItemsAndTax[@contextRef='OneD']"));
                getFloatValue(values, QuarterlyFilingsHeads.NON_PROMOTER_SHARES),
                getFloatValue(values, QuarterlyFilingsHeads.PROMOTER_SHARES),
                getFloatValue(values, QuarterlyFilingsHeads.PROMOTER_PERCENTAGE_OF_SHARES),
                getStringValue(values, QuarterlyFilingsHeads.NS_IEOI),
                getFloatValue(values, QuarterlyFilingsHeads.OPERATING_PROFIT),
                getFloatValue(values, QuarterlyFilingsHeads.OPERATING_PROFIT_MARGIN),
                getFloatValue(values, QuarterlyFilingsHeads.OTHER_INCOME),
                getFloatValue(values, QuarterlyFilingsHeads.PAT),
                getFloatValue(values, QuarterlyFilingsHeads.PBT),
                getFloatValue(values, QuarterlyFilingsHeads.RESERVES),
                getStringValue(values, QuarterlyFilingsHeads.RESULT_TYPE),
                getFloatValue(values, QuarterlyFilingsHeads.PROFIT_AND_LOSS_OF_ASSOCIATES),
                getFloatValue(values, QuarterlyFilingsHeads.TAX),
                getFloatValue(values, QuarterlyFilingsHeads.TOTAL_INCOME));


    }*/
    private void download() throws InterruptedException, FileNotFoundException, ParseException {
        File dir = new File("/data/datasets/qrjson");
        PrintWriter pw = new PrintWriter(new File("/tmp/keys.txt"));
        for (File f :dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith("json");
            }
        })) {
            download(f);
        }
        pw.close();
    }
    private void download(File f) throws FileNotFoundException, ParseException {

        MongoDatabase db = getMongoClient().getDatabase(database);
        MongoCollection<StockMaster> collection = db.getCollection(stockMasterCollectionName, StockMaster.class);
        MongoCollection<QuarterlyResults> qrCollection = db.getCollection(quarterlyResultsCollectionName, QuarterlyResults.class);
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
            StockMaster sm = collection.find(Filters.eq("symbol", table.symbol())).first();
            if (sm == null) {
                System.out.println("Could not find stock master for isin = " + table.isin() + " " + f.getAbsolutePath());
            }
            else {
                Date toDate = Date.from(table.toDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date fromDate = Date.from(table.fromDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                Bson query = Filters.and(Filters.eq("symbol", sm.getSymbol()), Filters.eq("periodStarting", fromDate));
                for (QuarterlyResults qr : qrCollection.find(query)) {
                    qr.setBroadcastTime(Date.from(table.broadCastDate().atZone(ZoneId.systemDefault()).toInstant()));
                    qrCollection.replaceOne(Filters.eq("_id", qr.getId()), qr);
                }
            }
        }
    }
/*    private void parseXml(File file) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.normalize();
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Map.Entry<String,String> e : xPathMap.entrySet()) {
            try {

                System.out.println(e.getKey() + " " + ((Node)xpath.evaluate(e.getValue(), doc, XPathConstants.NODE)).getTextContent());
            }
            catch (Exception ex) {
                System.out.println("Not found " + e.getKey());
            }
        }
    }*/
}
