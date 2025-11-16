package org.avasthi.java.cli;

import com.mongodb.client.MongoCollection;
import org.avasthi.java.cli.pojos.CorporateEvent;
import org.avasthi.java.cli.pojos.StockMaster;
import org.avasthi.java.cli.pojos.StockPrice;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadYahooFinance extends Base {
  private String mongoUrl = "mongodb://localhost";
  private final String[] nifty500Companies = { "360ONE",  "3MINDIA",  "ABB",  "ACC",  "AIAENG",  "APLAPOLLO",  "AUBANK",  "AADHARHFC",  "AARTIIND",  "AAVAS",  "ABBOTINDIA",  "ACE",  "ADANIENSOL",  "ADANIENT",  "ADANIGREEN",  "ADANIPORTS",  "ADANIPOWER",  "ATGL",  "AWL",  "ABCAPITAL",  "ABFRL",  "ABREL",  "ABSLAMC",  "AEGISLOG",  "AFFLE",  "AJANTPHARM",  "AKUMS",  "APLLTD",  "ALKEM",  "ALKYLAMINE",  "ALOKINDS",  "ARE&M",  "AMBER",  "AMBUJACEM",  "ANANDRATHI",  "ANANTRAJ",  "ANGELONE",  "APARINDS",  "APOLLOHOSP",  "APOLLOTYRE",  "APTUS",  "ACI",  "ASAHIINDIA",  "ASHOKLEY",  "ASIANPAINT",  "ASTERDM",  "ASTRAZEN",  "ASTRAL",  "ATUL",  "AUROPHARMA",  "AVANTIFEED",  "DMART",  "AXISBANK",  "BASF",  "BEML",  "BLS",  "BSE",  "BAJAJ-AUTO",  "BAJFINANCE",  "BAJAJFINSV",  "BAJAJHLDNG",  "BALAMINES",  "BALKRISIND",  "BALRAMCHIN",  "BANDHANBNK",  "BANKBARODA",  "BANKINDIA",  "MAHABANK",  "BATAINDIA",  "BAYERCROP",  "BERGEPAINT",  "BDL",  "BEL",  "BHARATFORG",  "BHEL",  "BPCL",  "BHARTIARTL",  "BHARTIHEXA",  "BIKAJI",  "BIOCON",  "BIRLACORPN",  "BSOFT",  "BLUEDART",  "BLUESTARCO",  "BBTC",  "BOSCHLTD",  "BRIGADE",  "BRITANNIA",  "MAPMYINDIA",  "CCL",  "CESC",  "CGPOWER",  "CIEINDIA",  "CRISIL",  "CAMPUS",  "CANFINHOME",  "CANBK",  "CAPLIPOINT",  "CGCL",  "CARBORUNIV",  "CASTROLIND",  "CEATLTD",  "CELLO",  "CENTRALBK",  "CDSL",  "CENTURYPLY",  "CERA",  "CHALET",  "CHAMBLFERT",  "CHEMPLASTS",  "CHENNPETRO",  "CHOLAHLDNG",  "CHOLAFIN",  "CIPLA",  "CUB",  "CLEAN",  "COALINDIA",  "COCHINSHIP",  "COFORGE",  "COLPAL",  "CAMS",  "CONCORDBIO",  "CONCOR",  "COROMANDEL",  "CRAFTSMAN",  "CREDITACC",  "CROMPTON",  "CUMMINSIND",  "CYIENT",  "DLF",  "DOMS",  "DABUR",  "DALBHARAT",  "DATAPATTNS",  "DEEPAKFERT",  "DEEPAKNTR",  "DELHIVERY",  "DEVYANI",  "DIVISLAB",  "DIXON",  "LALPATHLAB",  "DRREDDY",  "EIDPARRY",  "EIHOTEL",  "EASEMYTRIP",  "EICHERMOT",  "ELECON",  "ELGIEQUIP",  "EMAMILTD",  "EMCURE",  "ENDURANCE",  "ENGINERSIN",  "EQUITASBNK",  "ERIS",  "ESCORTS",  "EXIDEIND",  "NYKAA",  "FEDERALBNK",  "FACT",  "FINEORG",  "FINCABLES",  "FINPIPE",  "FSL",  "FIVESTAR",  "FORTIS",  "GRINFRA",  "GAIL",  "GVT&D",  "GMRINFRA",  "GRSE",  "GICRE",  "GILLETTE",  "GLAND",  "GLAXO",  "GLENMARK",  "MEDANTA",  "GODIGIT",  "GPIL",  "GODFRYPHLP",  "GODREJAGRO",  "GODREJCP",  "GODREJIND",  "GODREJPROP",  "GRANULES",  "GRAPHITE",  "GRASIM",  "GESHIP",  "GRINDWELL",  "GAEL",  "FLUOROCHEM",  "GUJGASLTD",  "GMDCLTD",  "GNFC",  "GPPL",  "GSFC",  "GSPL",  "HEG",  "HBLPOWER",  "HCLTECH",  "HDFCAMC",  "HDFCBANK",  "HDFCLIFE",  "HFCL",  "HAPPSTMNDS",  "HAVELLS",  "HEROMOTOCO",  "HSCL",  "HINDALCO",  "HAL",  "HINDCOPPER",  "HINDPETRO",  "HINDUNILVR",  "HINDZINC",  "POWERINDIA",  "HOMEFIRST",  "HONASA",  "HONAUT",  "HUDCO",  "ICICIBANK",  "ICICIGI",  "ICICIPRULI",  "ISEC",  "IDBI",  "IDFCFIRSTB",  "IFCI",  "IIFL",  "INOXINDIA",  "IRB",  "IRCON",  "ITC",  "ITI",  "INDGN",  "INDIACEM",  "INDIAMART",  "INDIANB",  "IEX",  "INDHOTEL",  "IOC",  "IOB",  "IRCTC",  "IRFC",  "IREDA",  "IGL",  "INDUSTOWER",  "INDUSINDBK",  "NAUKRI",  "INFY",  "INOXWIND",  "INTELLECT",  "INDIGO",  "IPCALAB",  "JBCHEPHARM",  "JKCEMENT",  "JBMA",  "JKLAKSHMI",  "JKTYRE",  "JMFINANCIL",  "JSWENERGY",  "JSWINFRA",  "JSWSTEEL",  "JPPOWER",  "J&KBANK",  "JINDALSAW",  "JSL",  "JINDALSTEL",  "JIOFIN",  "JUBLFOOD",  "JUBLINGREA",  "JUBLPHARMA",  "JWL",  "JUSTDIAL",  "JYOTHYLAB",  "JYOTICNC",  "KPRMILL",  "KEI",  "KNRCON",  "KPITTECH",  "KSB",  "KAJARIACER",  "KPIL",  "KALYANKJIL",  "KANSAINER",  "KARURVYSYA",  "KAYNES",  "KEC",  "KFINTECH",  "KIRLOSBROS",  "KIRLOSENG",  "KOTAKBANK",  "KIMS",  "LTF",  "LTTS",  "LICHSGFIN",  "LTIM",  "LT",  "LATENTVIEW",  "LAURUSLABS",  "LEMONTREE",  "LICI",  "LINDEINDIA",  "LLOYDSME",  "LUPIN",  "MMTC",  "MRF",  "LODHA",  "MGL",  "MAHSEAMLES",  "M&MFIN",  "M&M",  "MAHLIFE",  "MANAPPURAM",  "MRPL",  "MANKIND",  "MARICO",  "MARUTI",  "MASTEK",  "MFSL",  "MAXHEALTH",  "MAZDOCK",  "METROBRAND",  "METROPOLIS",  "MINDACORP",  "MSUMI",  "MOTILALOFS",  "MPHASIS",  "MCX",  "MUTHOOTFIN",  "NATCOPHARM",  "NBCC",  "NCC",  "NHPC",  "NLCINDIA",  "NMDC",  "NSLNISP",  "NTPC",  "NH",  "NATIONALUM",  "NAVINFLUOR",  "NESTLEIND",  "NETWEB",  "NETWORK18",  "NEWGEN",  "NAM-INDIA",  "NUVAMA",  "NUVOCO",  "OBEROIRLTY",  "ONGC",  "OIL",  "OLECTRA",  "PAYTM",  "OFSS",  "POLICYBZR",  "PCBL",  "PIIND",  "PNBHOUSING",  "PNCINFRA",  "PTCIL",  "PVRINOX",  "PAGEIND",  "PATANJALI",  "PERSISTENT",  "PETRONET",  "PFIZER",  "PHOENIXLTD",  "PIDILITIND",  "PEL",  "PPLPHARMA",  "POLYMED",  "POLYCAB",  "POONAWALLA",  "PFC",  "POWERGRID",  "PRAJIND",  "PRESTIGE",  "PGHH",  "PNB",  "QUESS",  "RRKABEL",  "RBLBANK",  "RECLTD",  "RHIM",  "RITES",  "RADICO",  "RVNL",  "RAILTEL",  "RAINBOW",  "RAJESHEXPO",  "RKFORGE",  "RCF",  "RATNAMANI",  "RTNINDIA",  "RAYMOND",  "REDINGTON",  "RELIANCE",  "ROUTE",  "SBFC",  "SBICARD",  "SBILIFE",  "SJVN",  "SKFINDIA",  "SRF",  "SAMMAANCAP",  "MOTHERSON",  "SANOFI",  "SAPPHIRE",  "SAREGAMA",  "SCHAEFFLER",  "SCHNEIDER",  "SCI",  "SHREECEM",  "RENUKA",  "SHRIRAMFIN",  "SHYAMMETL",  "SIEMENS",  "SIGNATURE",  "SOBHA",  "SOLARINDS",  "SONACOMS",  "SONATSOFTW",  "STARHEALTH",  "SBIN",  "SAIL",  "SWSOLAR",  "SUMICHEM",  "SPARC",  "SUNPHARMA",  "SUNTV",  "SUNDARMFIN",  "SUNDRMFAST",  "SUPREMEIND",  "SUVENPHAR",  "SUZLON",  "SWANENERGY",  "SYNGENE",  "SYRMA",  "TBOTEK",  "TVSMOTOR",  "TVSSCS",  "TANLA",  "TATACHEM",  "TATACOMM",  "TCS",  "TATACONSUM",  "TATAELXSI",  "TATAINVEST",  "TATAMOTORS",  "TATAPOWER",  "TATASTEEL",  "TATATECH",  "TTML",  "TECHM",  "TECHNOE",  "TEJASNET",  "NIACL",  "RAMCOCEM",  "THERMAX",  "TIMKEN",  "TITAGARH",  "TITAN",  "TORNTPHARM",  "TORNTPOWER",  "TRENT",  "TRIDENT",  "TRIVENI",  "TRITURBINE",  "TIINDIA",  "UCOBANK",  "UNOMINDA",  "UPL",  "UTIAMC",  "UJJIVANSFB",  "ULTRACEMCO",  "UNIONBANK",  "UBL",  "UNITDSPR",  "USHAMART",  "VGUARD",  "VIPIND",  "DBREALTY",  "VTL",  "VARROC",  "VBL",  "MANYAVAR",  "VEDL",  "VIJAYA",  "VINATIORGA",  "IDEA",  "VOLTAS",  "WELCORP",  "WELSPUNLIV",  "WESTLIFE",  "WHIRLPOOL",  "WIPRO",  "YESBANK",  "ZFCVINDIA",  "ZEEL",  "ZENSARTECH",  "ZOMATO",  "ZYDUSLIFE",  "ECLERX"};
  // private final String[] nifty500Companies = { "TATASTEEL", "TECHM", "RECLTD"};
//    private final String[] nifty500Companies = {   "IDBI",  "IDFCFIRSTB",  "IFCI",  "IIFL",  "INOXINDIA",  "IRB",  "IRCON",  "ITC",  "ITI",  "INDGN",  "INDIACEM",  "INDIAMART",  "INDIANB",  "IEX",  "INDHOTEL",  "IOC",  "IOB",  "IRCTC",  "IRFC",  "IREDA",  "IGL",  "INDUSTOWER",  "INDUSINDBK",  "NAUKRI",  "INFY",  "INOXWIND",  "INTELLECT",  "INDIGO",  "IPCALAB",  "JBCHEPHARM",  "JKCEMENT",  "JBMA",  "JKLAKSHMI",  "JKTYRE",  "JMFINANCIL",  "JSWENERGY",  "JSWINFRA",  "JSWSTEEL",  "JPPOWER",  "J&KBANK",  "JINDALSAW",  "JSL",  "JINDALSTEL",  "JIOFIN",  "JUBLFOOD",  "JUBLINGREA",  "JUBLPHARMA",  "JWL",  "JUSTDIAL",  "JYOTHYLAB",  "JYOTICNC",  "KPRMILL",  "KEI",  "KNRCON",  "KPITTECH",  "KSB",  "KAJARIACER",  "KPIL",  "KALYANKJIL",  "KANSAINER",  "KARURVYSYA",  "KAYNES",  "KEC",  "KFINTECH",  "KIRLOSBROS",  "KIRLOSENG",  "KOTAKBANK",  "KIMS",  "LTF",  "LTTS",  "LICHSGFIN",  "LTIM",  "LT",  "LATENTVIEW",  "LAURUSLABS",  "LEMONTREE",  "LICI",  "LINDEINDIA",  "LLOYDSME",  "LUPIN",  "MMTC",  "MRF",  "LODHA",  "MGL",  "MAHSEAMLES",  "M&MFIN",  "M&M",  "MAHLIFE",  "MANAPPURAM",  "MRPL",  "MANKIND",  "MARICO",  "MARUTI",  "MASTEK",  "MFSL",  "MAXHEALTH",  "MAZDOCK",  "METROBRAND",  "METROPOLIS",  "MINDACORP",  "MSUMI",  "MOTILALOFS",  "MPHASIS",  "MCX",  "MUTHOOTFIN",  "NATCOPHARM",  "NBCC",  "NCC",  "NHPC",  "NLCINDIA",  "NMDC",  "NSLNISP",  "NTPC",  "NH",  "NATIONALUM",  "NAVINFLUOR",  "NESTLEIND",  "NETWEB",  "NETWORK18",  "NEWGEN",  "NAM-INDIA",  "NUVAMA",  "NUVOCO",  "OBEROIRLTY",  "ONGC",  "OIL",  "OLECTRA",  "PAYTM",  "OFSS",  "POLICYBZR",  "PCBL",  "PIIND",  "PNBHOUSING",  "PNCINFRA",  "PTCIL",  "PVRINOX",  "PAGEIND",  "PATANJALI",  "PERSISTENT",  "PETRONET",  "PFIZER",  "PHOENIXLTD",  "PIDILITIND",  "PEL",  "PPLPHARMA",  "POLYMED",  "POLYCAB",  "POONAWALLA",  "PFC",  "POWERGRID",  "PRAJIND",  "PRESTIGE",  "PGHH",  "PNB",  "QUESS",  "RRKABEL",  "RBLBANK",  "RECLTD",  "RHIM",  "RITES",  "RADICO",  "RVNL",  "RAILTEL",  "RAINBOW",  "RAJESHEXPO",  "RKFORGE",  "RCF",  "RATNAMANI",  "RTNINDIA",  "RAYMOND",  "REDINGTON",  "RELIANCE",  "ROUTE",  "SBFC",  "SBICARD",  "SBILIFE",  "SJVN",  "SKFINDIA",  "SRF",  "SAMMAANCAP",  "MOTHERSON",  "SANOFI",  "SAPPHIRE",  "SAREGAMA",  "SCHAEFFLER",  "SCHNEIDER",  "SCI",  "SHREECEM",  "RENUKA",  "SHRIRAMFIN",  "SHYAMMETL",  "SIEMENS",  "SIGNATURE",  "SOBHA",  "SOLARINDS",  "SONACOMS",  "SONATSOFTW",  "STARHEALTH",  "SBIN",  "SAIL",  "SWSOLAR",  "SUMICHEM",  "SPARC",  "SUNPHARMA",  "SUNTV",  "SUNDARMFIN",  "SUNDRMFAST",  "SUPREMEIND",  "SUVENPHAR",  "SUZLON",  "SWANENERGY",  "SYNGENE",  "SYRMA",  "TBOTEK",  "TVSMOTOR",  "TVSSCS",  "TANLA",  "TATACHEM",  "TATACOMM",  "TCS",  "TATACONSUM",  "TATAELXSI",  "TATAINVEST",  "TATAMOTORS",  "TATAPOWER",  "TATASTEEL",  "TATATECH",  "TTML",  "TECHM",  "TECHNOE",  "TEJASNET",  "NIACL",  "RAMCOCEM",  "THERMAX",  "TIMKEN",  "TITAGARH",  "TITAN",  "TORNTPHARM",  "TORNTPOWER",  "TRENT",  "TRIDENT",  "TRIVENI",  "TRITURBINE",  "TIINDIA",  "UCOBANK",  "UNOMINDA",  "UPL",  "UTIAMC",  "UJJIVANSFB",  "ULTRACEMCO",  "UNIONBANK",  "UBL",  "UNITDSPR",  "USHAMART",  "VGUARD",  "VIPIND",  "DBREALTY",  "VTL",  "VARROC",  "VBL",  "MANYAVAR",  "VEDL",  "VIJAYA",  "VINATIORGA",  "IDEA",  "VOLTAS",  "WELCORP",  "WELSPUNLIV",  "WESTLIFE",  "WHIRLPOOL",  "WIPRO",  "YESBANK",  "ZFCVINDIA",  "ZEEL",  "ZENSARTECH",  "ZOMATO",  "ZYDUSLIFE",  "ECLERX"};
//    private final String[] nifty500Companies = { "SIGNATURE",  "SOBHA",  "SOLARINDS",  "SONACOMS",  "SONATSOFTW",  "STARHEALTH",  "SBIN",  "SAIL",  "SWSOLAR",  "SUMICHEM",  "SPARC",  "SUNPHARMA",  "SUNTV",  "SUNDARMFIN",  "SUNDRMFAST",  "SUPREMEIND",  "SUVENPHAR",  "SUZLON",  "SWANENERGY",  "SYNGENE",  "SYRMA",  "TBOTEK",  "TVSMOTOR",  "TVSSCS",  "TANLA",  "TATACHEM",  "TATACOMM",  "TCS",  "TATACONSUM",  "TATAELXSI",  "TATAINVEST",  "TATAMOTORS",  "TATAPOWER",  "TATASTEEL",  "TATATECH",  "TTML",  "TECHM",  "TECHNOE",  "TEJASNET",  "NIACL",  "RAMCOCEM",  "THERMAX",  "TIMKEN",  "TITAGARH",  "TITAN",  "TORNTPHARM",  "TORNTPOWER",  "TRENT",  "TRIDENT",  "TRIVENI",  "TRITURBINE",  "TIINDIA",  "UCOBANK",  "UNOMINDA",  "UPL",  "UTIAMC",  "UJJIVANSFB",  "ULTRACEMCO",  "UNIONBANK",  "UBL",  "UNITDSPR",  "USHAMART",  "VGUARD",  "VIPIND",  "DBREALTY",  "VTL",  "VARROC",  "VBL",  "MANYAVAR",  "VEDL",  "VIJAYA",  "VINATIORGA",  "IDEA",  "VOLTAS",  "WELCORP",  "WELSPUNLIV",  "WESTLIFE",  "WHIRLPOOL",  "WIPRO",  "YESBANK",  "ZFCVINDIA",  "ZEEL",  "ZENSARTECH",  "ZOMATO",  "ZYDUSLIFE",  "ECLERX"};
  public static void main(String[] args) throws IOException, InterruptedException {

    DownloadYahooFinance dbc = new DownloadYahooFinance();
    dbc.downloadStockPrices();
  }
  private void downloadStockPrices() throws InterruptedException, IOException {
    for (StockMaster sm : getStockMasterCollection().find().sort(new Document("symbol", 1))) {
      if (sm.isNifty50() || sm.isSensex()) {

        downloadStockPrices(sm.getSymbol(), "EQ");
      }
      else {
        System.out.println("Skipping " + sm.getSymbol() + "...");
      }
    }
  }
  private void downloadStockPrices(String symbol,
                                   String series) throws InterruptedException, IOException {
    WebDriver driver = getChromeDriver(true);
    try {

      String url = String.format("https://finance.yahoo.com/quote/%s.NS/history/?period1=1758672000&period2=1763164800", symbol);
      driver.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(2)).implicitlyWait(Duration.ofMinutes(1));
      System.out.print("Loading " + symbol + " ");
      driver.get(url);
      System.out.println(url);
//      WebElement input = new WebDriverWait(driver, Duration.ofMinutes(100)).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[2]/main/section/section/section/section/section[2]/header/h3")));
      WebElement element = driver.findElement(By.xpath("/html/body/div[2]/main/section/section/section/section/div[1]/div[3]/table"));
      DateTimeFormatter dateFormat = new DateTimeFormatterBuilder()
              .parseCaseInsensitive()
              .parseLenient()
              .appendPattern("MMM d, yyyy")
              .toFormatter(Locale.ENGLISH);
      ZoneOffset currentZoneOffset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
      List<WebElement> rows = element.findElements(By.cssSelector(".table > tbody > tr"));
      List<StockPrice> stockPriceList = new ArrayList<>();
      List<CorporateEvent>corporateEventList = new ArrayList<>();
      for (WebElement row : rows) {
        List<WebElement> tds = row.findElements(By.cssSelector(".table > tbody > tr > td"));
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
          StockPrice sp = new StockPrice(UUID.randomUUID(), symbol, date, series, open, high, low, close, adjustedClose, volume == null ? 0 : volume);
          stockPriceList.add(sp);
          if (stockPriceList.size() > 10000) {
            insertRecords(stockPriceList);
          }
        }
      }
      if (corporateEventList.size() > 0) {
        insertCorporateEvents(corporateEventList);
      }
      if (stockPriceList.size() > 0) {
        insertRecords(stockPriceList);
      }
    }
    catch (org.openqa.selenium.NoSuchElementException ex) {
      ex.printStackTrace();
    }
    System.out.println(String.format("Finished %s", symbol));
    driver.quit();
  }
  private void insertCorporateEvents(List<CorporateEvent> corporateEventList) {

    MongoCollection<CorporateEvent> collection = getCorporateEventsCollection();
    try {

      collection.insertMany(corporateEventList);
    }
    catch (Exception e) {
      corporateEventList.forEach( ce -> {
        try {

          collection.insertOne(ce);
        }
        catch (Exception e1) {

          System.out.println("Insert Corporate Events failed..");
        }
      });
    }
    corporateEventList.clear();
  }
  private void insertRecords(List<StockPrice> stockPriceList) {

    MongoCollection<StockPrice> collection = getStockPriceCollection();
    try {

      collection.insertMany(stockPriceList);
    }
    catch (Exception e) {
      stockPriceList.forEach( sp -> {
        try {

          collection.insertOne(sp);
        }
        catch (Exception e1) {
          System.out.println("Insert Stock Price failed..");
        }
      });
    }
    stockPriceList.clear();
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