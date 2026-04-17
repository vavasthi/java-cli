package org.avasthi.java.cli;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class LongStraddlePriceVariation extends Base {

  public static void main(String[] args) throws IOException, InterruptedException, ParseException, CsvException {
    LongStraddlePriceVariation lqr = new LongStraddlePriceVariation();
    lqr.computePriceVariation();
  }
  private void computePriceVariation() throws IOException, ParseException {
    for (double d = .15; d < .35; d+=.01) {

      computePriceVariation(d);
    }
  }
  private void computePriceVariation(double iv) throws ParseException, IOException {
    System.out.println(String.format("Price variation at IV of %.2f", iv));
    Calendar calendar = Calendar.getInstance();
    calendar.set(2026,Calendar.APRIL, 13, 15, 30, 0);
    double strike = 23000;
    for (double movement = -150; movement <= 150; movement += 10) {

      double callDelta = ImpliedVolatility.calculateDelta(strike + movement, strike, calendar.getTime(), .1, iv, true);
      double putDelta = ImpliedVolatility.calculateDelta(strike + movement, strike, calendar.getTime(), .1, iv, false);
      System.out.println(String.format("Price Change at movement %.2f call %.2f put %.2f profit %.2f", movement, movement*callDelta, movement*putDelta, movement * (callDelta + putDelta)));
    }
  }
}