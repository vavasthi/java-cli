package org.avasthi.java.cli;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.avasthi.java.cli.pojos.CorporateEvent;
import org.avasthi.java.cli.pojos.StockPrice;
import org.bson.UuidRepresentation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadYahooFinance extends Base {
    private String mongoUrl = "mongodb://localhost";
//    private final String[] nifty500Companies = { "360ONE",  "3MINDIA",  "ABB",  "ACC",  "AIAENG",  "APLAPOLLO",  "AUBANK",  "AADHARHFC",  "AARTIIND",  "AAVAS",  "ABBOTINDIA",  "ACE",  "ADANIENSOL",  "ADANIENT",  "ADANIGREEN",  "ADANIPORTS",  "ADANIPOWER",  "ATGL",  "AWL",  "ABCAPITAL",  "ABFRL",  "ABREL",  "ABSLAMC",  "AEGISLOG",  "AFFLE",  "AJANTPHARM",  "AKUMS",  "APLLTD",  "ALKEM",  "ALKYLAMINE",  "ALOKINDS",  "ARE&M",  "AMBER",  "AMBUJACEM",  "ANANDRATHI",  "ANANTRAJ",  "ANGELONE",  "APARINDS",  "APOLLOHOSP",  "APOLLOTYRE",  "APTUS",  "ACI",  "ASAHIINDIA",  "ASHOKLEY",  "ASIANPAINT",  "ASTERDM",  "ASTRAZEN",  "ASTRAL",  "ATUL",  "AUROPHARMA",  "AVANTIFEED",  "DMART",  "AXISBANK",  "BASF",  "BEML",  "BLS",  "BSE",  "BAJAJ-AUTO",  "BAJFINANCE",  "BAJAJFINSV",  "BAJAJHLDNG",  "BALAMINES",  "BALKRISIND",  "BALRAMCHIN",  "BANDHANBNK",  "BANKBARODA",  "BANKINDIA",  "MAHABANK",  "BATAINDIA",  "BAYERCROP",  "BERGEPAINT",  "BDL",  "BEL",  "BHARATFORG",  "BHEL",  "BPCL",  "BHARTIARTL",  "BHARTIHEXA",  "BIKAJI",  "BIOCON",  "BIRLACORPN",  "BSOFT",  "BLUEDART",  "BLUESTARCO",  "BBTC",  "BOSCHLTD",  "BRIGADE",  "BRITANNIA",  "MAPMYINDIA",  "CCL",  "CESC",  "CGPOWER",  "CIEINDIA",  "CRISIL",  "CAMPUS",  "CANFINHOME",  "CANBK",  "CAPLIPOINT",  "CGCL",  "CARBORUNIV",  "CASTROLIND",  "CEATLTD",  "CELLO",  "CENTRALBK",  "CDSL",  "CENTURYPLY",  "CERA",  "CHALET",  "CHAMBLFERT",  "CHEMPLASTS",  "CHENNPETRO",  "CHOLAHLDNG",  "CHOLAFIN",  "CIPLA",  "CUB",  "CLEAN",  "COALINDIA",  "COCHINSHIP",  "COFORGE",  "COLPAL",  "CAMS",  "CONCORDBIO",  "CONCOR",  "COROMANDEL",  "CRAFTSMAN",  "CREDITACC",  "CROMPTON",  "CUMMINSIND",  "CYIENT",  "DLF",  "DOMS",  "DABUR",  "DALBHARAT",  "DATAPATTNS",  "DEEPAKFERT",  "DEEPAKNTR",  "DELHIVERY",  "DEVYANI",  "DIVISLAB",  "DIXON",  "LALPATHLAB",  "DRREDDY",  "EIDPARRY",  "EIHOTEL",  "EASEMYTRIP",  "EICHERMOT",  "ELECON",  "ELGIEQUIP",  "EMAMILTD",  "EMCURE",  "ENDURANCE",  "ENGINERSIN",  "EQUITASBNK",  "ERIS",  "ESCORTS",  "EXIDEIND",  "NYKAA",  "FEDERALBNK",  "FACT",  "FINEORG",  "FINCABLES",  "FINPIPE",  "FSL",  "FIVESTAR",  "FORTIS",  "GRINFRA",  "GAIL",  "GVT&D",  "GMRINFRA",  "GRSE",  "GICRE",  "GILLETTE",  "GLAND",  "GLAXO",  "GLENMARK",  "MEDANTA",  "GODIGIT",  "GPIL",  "GODFRYPHLP",  "GODREJAGRO",  "GODREJCP",  "GODREJIND",  "GODREJPROP",  "GRANULES",  "GRAPHITE",  "GRASIM",  "GESHIP",  "GRINDWELL",  "GAEL",  "FLUOROCHEM",  "GUJGASLTD",  "GMDCLTD",  "GNFC",  "GPPL",  "GSFC",  "GSPL",  "HEG",  "HBLPOWER",  "HCLTECH",  "HDFCAMC",  "HDFCBANK",  "HDFCLIFE",  "HFCL",  "HAPPSTMNDS",  "HAVELLS",  "HEROMOTOCO",  "HSCL",  "HINDALCO",  "HAL",  "HINDCOPPER",  "HINDPETRO",  "HINDUNILVR",  "HINDZINC",  "POWERINDIA",  "HOMEFIRST",  "HONASA",  "HONAUT",  "HUDCO",  "ICICIBANK",  "ICICIGI",  "ICICIPRULI",  "ISEC",  "IDBI",  "IDFCFIRSTB",  "IFCI",  "IIFL",  "INOXINDIA",  "IRB",  "IRCON",  "ITC",  "ITI",  "INDGN",  "INDIACEM",  "INDIAMART",  "INDIANB",  "IEX",  "INDHOTEL",  "IOC",  "IOB",  "IRCTC",  "IRFC",  "IREDA",  "IGL",  "INDUSTOWER",  "INDUSINDBK",  "NAUKRI",  "INFY",  "INOXWIND",  "INTELLECT",  "INDIGO",  "IPCALAB",  "JBCHEPHARM",  "JKCEMENT",  "JBMA",  "JKLAKSHMI",  "JKTYRE",  "JMFINANCIL",  "JSWENERGY",  "JSWINFRA",  "JSWSTEEL",  "JPPOWER",  "J&KBANK",  "JINDALSAW",  "JSL",  "JINDALSTEL",  "JIOFIN",  "JUBLFOOD",  "JUBLINGREA",  "JUBLPHARMA",  "JWL",  "JUSTDIAL",  "JYOTHYLAB",  "JYOTICNC",  "KPRMILL",  "KEI",  "KNRCON",  "KPITTECH",  "KSB",  "KAJARIACER",  "KPIL",  "KALYANKJIL",  "KANSAINER",  "KARURVYSYA",  "KAYNES",  "KEC",  "KFINTECH",  "KIRLOSBROS",  "KIRLOSENG",  "KOTAKBANK",  "KIMS",  "LTF",  "LTTS",  "LICHSGFIN",  "LTIM",  "LT",  "LATENTVIEW",  "LAURUSLABS",  "LEMONTREE",  "LICI",  "LINDEINDIA",  "LLOYDSME",  "LUPIN",  "MMTC",  "MRF",  "LODHA",  "MGL",  "MAHSEAMLES",  "M&MFIN",  "M&M",  "MAHLIFE",  "MANAPPURAM",  "MRPL",  "MANKIND",  "MARICO",  "MARUTI",  "MASTEK",  "MFSL",  "MAXHEALTH",  "MAZDOCK",  "METROBRAND",  "METROPOLIS",  "MINDACORP",  "MSUMI",  "MOTILALOFS",  "MPHASIS",  "MCX",  "MUTHOOTFIN",  "NATCOPHARM",  "NBCC",  "NCC",  "NHPC",  "NLCINDIA",  "NMDC",  "NSLNISP",  "NTPC",  "NH",  "NATIONALUM",  "NAVINFLUOR",  "NESTLEIND",  "NETWEB",  "NETWORK18",  "NEWGEN",  "NAM-INDIA",  "NUVAMA",  "NUVOCO",  "OBEROIRLTY",  "ONGC",  "OIL",  "OLECTRA",  "PAYTM",  "OFSS",  "POLICYBZR",  "PCBL",  "PIIND",  "PNBHOUSING",  "PNCINFRA",  "PTCIL",  "PVRINOX",  "PAGEIND",  "PATANJALI",  "PERSISTENT",  "PETRONET",  "PFIZER",  "PHOENIXLTD",  "PIDILITIND",  "PEL",  "PPLPHARMA",  "POLYMED",  "POLYCAB",  "POONAWALLA",  "PFC",  "POWERGRID",  "PRAJIND",  "PRESTIGE",  "PGHH",  "PNB",  "QUESS",  "RRKABEL",  "RBLBANK",  "RECLTD",  "RHIM",  "RITES",  "RADICO",  "RVNL",  "RAILTEL",  "RAINBOW",  "RAJESHEXPO",  "RKFORGE",  "RCF",  "RATNAMANI",  "RTNINDIA",  "RAYMOND",  "REDINGTON",  "RELIANCE",  "ROUTE",  "SBFC",  "SBICARD",  "SBILIFE",  "SJVN",  "SKFINDIA",  "SRF",  "SAMMAANCAP",  "MOTHERSON",  "SANOFI",  "SAPPHIRE",  "SAREGAMA",  "SCHAEFFLER",  "SCHNEIDER",  "SCI",  "SHREECEM",  "RENUKA",  "SHRIRAMFIN",  "SHYAMMETL",  "SIEMENS",  "SIGNATURE",  "SOBHA",  "SOLARINDS",  "SONACOMS",  "SONATSOFTW",  "STARHEALTH",  "SBIN",  "SAIL",  "SWSOLAR",  "SUMICHEM",  "SPARC",  "SUNPHARMA",  "SUNTV",  "SUNDARMFIN",  "SUNDRMFAST",  "SUPREMEIND",  "SUVENPHAR",  "SUZLON",  "SWANENERGY",  "SYNGENE",  "SYRMA",  "TBOTEK",  "TVSMOTOR",  "TVSSCS",  "TANLA",  "TATACHEM",  "TATACOMM",  "TCS",  "TATACONSUM",  "TATAELXSI",  "TATAINVEST",  "TATAMOTORS",  "TATAPOWER",  "TATASTEEL",  "TATATECH",  "TTML",  "TECHM",  "TECHNOE",  "TEJASNET",  "NIACL",  "RAMCOCEM",  "THERMAX",  "TIMKEN",  "TITAGARH",  "TITAN",  "TORNTPHARM",  "TORNTPOWER",  "TRENT",  "TRIDENT",  "TRIVENI",  "TRITURBINE",  "TIINDIA",  "UCOBANK",  "UNOMINDA",  "UPL",  "UTIAMC",  "UJJIVANSFB",  "ULTRACEMCO",  "UNIONBANK",  "UBL",  "UNITDSPR",  "USHAMART",  "VGUARD",  "VIPIND",  "DBREALTY",  "VTL",  "VARROC",  "VBL",  "MANYAVAR",  "VEDL",  "VIJAYA",  "VINATIORGA",  "IDEA",  "VOLTAS",  "WELCORP",  "WELSPUNLIV",  "WESTLIFE",  "WHIRLPOOL",  "WIPRO",  "YESBANK",  "ZFCVINDIA",  "ZEEL",  "ZENSARTECH",  "ZOMATO",  "ZYDUSLIFE",  "ECLERX"};
    private final String[] nifty500Companies = { "SIGNATURE",  "SOBHA",  "SOLARINDS",  "SONACOMS",  "SONATSOFTW",  "STARHEALTH",  "SBIN",  "SAIL",  "SWSOLAR",  "SUMICHEM",  "SPARC",  "SUNPHARMA",  "SUNTV",  "SUNDARMFIN",  "SUNDRMFAST",  "SUPREMEIND",  "SUVENPHAR",  "SUZLON",  "SWANENERGY",  "SYNGENE",  "SYRMA",  "TBOTEK",  "TVSMOTOR",  "TVSSCS",  "TANLA",  "TATACHEM",  "TATACOMM",  "TCS",  "TATACONSUM",  "TATAELXSI",  "TATAINVEST",  "TATAMOTORS",  "TATAPOWER",  "TATASTEEL",  "TATATECH",  "TTML",  "TECHM",  "TECHNOE",  "TEJASNET",  "NIACL",  "RAMCOCEM",  "THERMAX",  "TIMKEN",  "TITAGARH",  "TITAN",  "TORNTPHARM",  "TORNTPOWER",  "TRENT",  "TRIDENT",  "TRIVENI",  "TRITURBINE",  "TIINDIA",  "UCOBANK",  "UNOMINDA",  "UPL",  "UTIAMC",  "UJJIVANSFB",  "ULTRACEMCO",  "UNIONBANK",  "UBL",  "UNITDSPR",  "USHAMART",  "VGUARD",  "VIPIND",  "DBREALTY",  "VTL",  "VARROC",  "VBL",  "MANYAVAR",  "VEDL",  "VIJAYA",  "VINATIORGA",  "IDEA",  "VOLTAS",  "WELCORP",  "WELSPUNLIV",  "WESTLIFE",  "WHIRLPOOL",  "WIPRO",  "YESBANK",  "ZFCVINDIA",  "ZEEL",  "ZENSARTECH",  "ZOMATO",  "ZYDUSLIFE",  "ECLERX"};
    public static void main(String[] args) throws IOException, InterruptedException {

        DownloadYahooFinance dbc = new DownloadYahooFinance();
        dbc.downloadStockPrices();
    }
    private void downloadStockPrices() throws InterruptedException, IOException {
        WebDriver driver = getWebDriver();
        for (String symbol : nifty500Companies) {
            downloadStockPrices(driver, symbol, "EQ");
        }
        driver.quit();
    }
    private void downloadStockPrices(WebDriver driver,
                                     String symbol,
                                     String series) throws InterruptedException, IOException {
        String url = String.format("https://finance.yahoo.com/quote/%s.NS/history/?period1=0&period2=1731840127", symbol);
                driver.get(url);
        WebElement element = driver.findElement(By.xpath("//*[@id=\"nimbus-app\"]/section/section/section/article/div[1]/div[3]/table"));
        WebElement tHeadTr = element.findElement(By.xpath("//*[@id=\"nimbus-app\"]/section/section/section/article/div[1]/div[3]/table/thead/tr"));
        List<WebElement> thes = tHeadTr.findElements(By.cssSelector("#nimbus-app > section > section > section > article > div.container > div.table-container.yf-j5d1ld > table > thead > tr > th"));
        DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .parseLenient()
                .appendPattern("MMM d, yyyy")
                .toFormatter(Locale.ENGLISH);
        ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
        List<WebElement> rows = element.findElements(By.cssSelector("#nimbus-app > section > section > section > article > div.container > div.table-container.yf-j5d1ld > table > tbody > tr"));
        List<StockPrice> stockPriceList = new ArrayList<>();
        List<CorporateEvent>corporateEventList = new ArrayList<>();
        for (WebElement row : rows) {
            List<WebElement> tds = row.findElements(By.cssSelector("#nimbus-app > section > section > section > article > div.container > div.table-container.yf-j5d1ld > table > tbody > tr > td"));
            String f1 = tds.get(0).getText();
            Date date = Date.from(LocalDate.parse(f1, dateFormat).atStartOfDay().toInstant(currentZoneOffset));
            if(tds.size() != 7) {
                System.out.println(tds.size() + " " + tds.get(0).getText() + " " + tds.get(1).getText());
                addCorporateEvent(date, symbol, tds.get(1).getText(), corporateEventList);
                if (corporateEventList.size() > 20) {
                    getCorporateEventsCollection().insertMany(corporateEventList);
                    corporateEventList.clear();
                }
            }
            else {
                Float open = Float.parseFloat(tds.get(1).getText().replace(",",""));
                Float high = Float.parseFloat(tds.get(2).getText().replace(",",""));
                Float low = Float.parseFloat(tds.get(3).getText().replace(",",""));
                Float close = Float.parseFloat(tds.get(4).getText().replace(",",""));
                Float adjustedClose = Float.parseFloat(tds.get(5).getText().replace(",",""));
                Long volume = null;
                try {

                    volume = Long.parseLong(tds.get(6).getText().replace(",",""));
                }
                catch (NumberFormatException nfe) {

                }
//                System.out.println(String.format("%s,%f,%f,%f,%f,%f,%d",date, open, high, low, close, adjustedClose, volume));
                StockPrice sp = new StockPrice(UUID.randomUUID(), symbol, date, series, open, high, low, close, adjustedClose, volume);
                stockPriceList.add(sp);
                if (stockPriceList.size() > 500) {
                    getStockPriceCollection().insertMany(stockPriceList);
                    stockPriceList.clear();
                }
            }
        }
        if (corporateEventList.size() > 0) {
            getCorporateEventsCollection().insertMany(corporateEventList);
            corporateEventList.clear();
        }
        if (stockPriceList.size() > 0) {
            getStockPriceCollection().insertMany(stockPriceList);
            stockPriceList.clear();
        }
        System.out.println(String.format("Finished %s", symbol));
    }
    private MongoCollection<StockPrice> getStockPriceCollection() {
        return getMongoClient().getDatabase("capitalMarkets").getCollection("stockPrice", StockPrice.class);
    }
    private MongoCollection<CorporateEvent> getCorporateEventsCollection() {
        return getMongoClient().getDatabase("capitalMarkets").getCollection("corporateEvents", CorporateEvent.class);
    }
    private void addCorporateEvent(Date date, String symbol, String purpose, List<CorporateEvent> corporateEventList) {
        Float dividend = null;
        Float bonus = null;
        {
            Pattern dividendPattern = Pattern.compile("^(\\d*\\.\\d+|\\d+\\.\\d*) Dividend", Pattern.CASE_INSENSITIVE);
            Matcher matcher = dividendPattern.matcher(purpose);
            if (matcher.find()) {
                dividend = Float.parseFloat(matcher.group(1));
                System.out.println(purpose + " --> " + dividend);
            }
        }
        {
            Pattern dividendPattern = Pattern.compile("^(\\d*):(\\d+) Stock Splits", Pattern.CASE_INSENSITIVE);
            Matcher matcher = dividendPattern.matcher(purpose);
            if (matcher.find()) {
                float p1 = Float.parseFloat(matcher.group(1));
                float p2 = Float.parseFloat(matcher.group(2));
                bonus = p1 / p2;
                System.out.println(purpose + " --> " + bonus);
            }
        }
        if (dividend != null || bonus != null) {
            corporateEventList.add(new CorporateEvent(UUID.randomUUID(), symbol, date, bonus, dividend));
        }
    }
}