package org.avasthi.java.cli;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.*;
import com.opencsv.exceptions.CsvException;
import org.avasthi.java.cli.pojos.SimulatedLongStraddle;
import org.avasthi.java.cli.pojos.SimulatedTrade;
import org.avasthi.java.cli.pojos.TradeTick;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TestClass extends Base {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    TestClass lqr = new TestClass();
    lqr.loadVix();
  }
  private void loadVix() throws IOException, ParseException {
    Calendar calendar = Calendar.getInstance();
    Date to = calendar.getTime();
    calendar.add(Calendar.YEAR, -1);
    Date from = calendar.getTime();
    populateIndiaVix(from, to);
  }
}