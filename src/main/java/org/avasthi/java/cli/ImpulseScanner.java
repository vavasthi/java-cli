package org.avasthi.java.cli;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import org.avasthi.java.cli.pojos.TradeTick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class ImpulseScanner extends Base {

    public static void main(String[] args) throws FileNotFoundException {
      ImpulseScanner is = new ImpulseScanner();
      is.run();
    }
  void run() throws FileNotFoundException {
    PrintWriter writer = new PrintWriter(new File("macd.csv"));
    List<TradeTick> ticks = new LinkedList<>();

    getTradeTickCollection().find(Filters.eq("symbol", "NIFTY2632422600PE")).sort(Sorts.ascending("exchangeTimestamp")).forEach(tt-> {
      ticks.add(tt);
        if (ticks.size() >= 500) {

          List<ImpulseMACDEngine.CandleReport> reports =
                  ImpulseMACDEngine.generate(ticks, 5, 35, 5);
//                  ImpulseMACDEngine.generate(ticks, 34, 9, 30);


          ImpulseMACDEngine.CandleReport report = reports.getLast();
          if (report.tradeSignal() != ImpulseMACDEngine.TradeSignal.NONE) {
            System.out.println(ticks.getLast().exchangeTimestamp() + "," + report);
          }
        ticks.removeFirst();
        }
    });
    writer.close();
  }

}
