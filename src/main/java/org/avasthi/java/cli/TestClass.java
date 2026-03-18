package org.avasthi.java.cli;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

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