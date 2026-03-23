package org.avasthi.java.cli;

import org.avasthi.java.cli.pojos.TradeTick;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import java.util.*;
import java.util.stream.*;

public final class ImpulseMACDEngine {

  // Immutable Data Transfer Object
  public record CandleReport(
          double macd, double signal, double histogram,
          TrendColor color, TradeSignal tradeSignal, Divergence divergence
  ) {}

  public enum TrendColor { LIME, GREEN, ORANGE, RED }
  public enum TradeSignal { BUY, SELL, NONE }
  public enum Divergence { REGULAR_BULLISH, REGULAR_BEARISH, HIDDEN_BULLISH, HIDDEN_BEARISH, NONE }

  public static List<CandleReport> generate(List<TradeTick> ticks,
                                            int lenMA, int lenSig, int divLookback) {
    List<Double> high = new ArrayList<>();
    List<Double> low  = new ArrayList<>();
    List<Double> close = new ArrayList<>();

      ticks.stream().forEach(t -> {
        high.add((double)t.high());
        low.add((double)t.low());
        close.add((double)t.close());
      });

    int n = close.size();

    // 1. Source: HLC3 via Streams
    List<Double> hlc3 = IntStream.range(0, n)
            .mapToDouble(i -> (high.get(i) + low.get(i) + close.get(i)) / 3.0)
            .boxed().toList();

    // 2. Component Calculations
    List<Double> hi = calculateSMMA(high, lenMA);
    List<Double> lo = calculateSMMA(low, lenMA);
    List<Double> mi = calculateZLEMA(hlc3, lenMA);

    // 3. Impulse MACD Line
    List<Double> impulseMACD = IntStream.range(0, n).mapToDouble(i -> {
      if (mi.get(i) > hi.get(i)) return mi.get(i) - hi.get(i);
      if (mi.get(i) < lo.get(i)) return mi.get(i) - lo.get(i);
      return 0.0;
    }).boxed().toList();

    // 4. Signal Line
    List<Double> signalLine = calculateSMA(impulseMACD, lenSig);

    // 5. Final Assembly
    return IntStream.range(0, n).mapToObj(i -> {
      TrendColor color = hlc3.get(i) > mi.get(i)
              ? (hlc3.get(i) > hi.get(i) ? TrendColor.LIME : TrendColor.GREEN)
              : (hlc3.get(i) < lo.get(i) ? TrendColor.RED : TrendColor.ORANGE);

      TradeSignal signal = (i > 0) ? detectCrossover(impulseMACD, signalLine, i) : TradeSignal.NONE;
      Divergence div = detectDivergence(hlc3, impulseMACD, i, divLookback);

      return new CandleReport(
              impulseMACD.get(i), signalLine.get(i),
              impulseMACD.get(i) - signalLine.get(i),
              color, signal, div
      );
    }).toList();
  }

  private static List<Double> calculateZLEMA(List<Double> src, int len) {
    List<Double> e1 = calculateEMA(src, len);
    List<Double> e2 = calculateEMA(e1, len);
    return IntStream.range(0, src.size())
            .mapToDouble(i -> Math.fma(2.0, e1.get(i), -e2.get(i)))
            .boxed().toList();
  }

  private static List<Double> calculateEMA(List<Double> src, int len) {
    int n = src.size();
    double[] ema = new double[n];
    double alpha = 2.0 / (len + 1);
    double complement = 1.0 - alpha;

    ema[0] = src.get(0);
    for (int i = 1; i < n; i++) {
      ema[i] = Math.fma(alpha, src.get(i), complement * ema[i-1]);
    }
    return DoubleStream.of(ema).boxed().toList();
  }

  private static List<Double> calculateSMMA(List<Double> src, int len) {
    int n = src.size();
    double[] smma = new double[n];
    double firstSma = src.stream().limit(len).mapToDouble(d -> d).average().orElse(0.0);

    smma[len - 1] = firstSma;
    for (int i = len; i < n; i++) {
      smma[i] = Math.fma(smma[i-1], len - 1, src.get(i)) / len;
    }
    return DoubleStream.of(smma).boxed().toList();
  }

  private static List<Double> calculateSMA(List<Double> src, int len) {
    return IntStream.range(0, src.size())
            .mapToDouble(i -> i < len - 1 ? 0.0 :
                    src.subList(i - len + 1, i + 1).stream().mapToDouble(d -> d).average().orElse(0.0))
            .boxed().toList();
  }

  private static TradeSignal detectCrossover(List<Double> macd, List<Double> sig, int i) {
    if (macd.get(i-1) <= sig.get(i-1) && macd.get(i) > sig.get(i)) return TradeSignal.BUY;
    if (macd.get(i-1) >= sig.get(i-1) && macd.get(i) < sig.get(i)) return TradeSignal.SELL;
    return TradeSignal.NONE;
  }

  private static Divergence detectDivergence(List<Double> price, List<Double> osc, int i, int lookback) {
    if (i < 2 || i >= price.size() - 1) return Divergence.NONE;

    boolean isHigh = osc.get(i) > osc.get(i-1) && osc.get(i) > osc.get(i+1);
    boolean isLow = osc.get(i) < osc.get(i-1) && osc.get(i) < osc.get(i+1);

    if (isHigh) {
      int prev = findPrevPivot(osc, i - 1, lookback, true);
      if (prev != -1 && price.get(i) > price.get(prev) && osc.get(i) < osc.get(prev)) return Divergence.REGULAR_BEARISH;
      if (prev != -1 && price.get(i) < price.get(prev) && osc.get(i) > osc.get(prev)) return Divergence.HIDDEN_BEARISH;
    }
    if (isLow) {
      int prev = findPrevPivot(osc, i - 1, lookback, false);
      if (prev != -1 && price.get(i) < price.get(prev) && osc.get(i) > osc.get(prev)) return Divergence.REGULAR_BULLISH;
      if (prev != -1 && price.get(i) > price.get(prev) && osc.get(i) < osc.get(prev)) return Divergence.HIDDEN_BULLISH;
    }
    return Divergence.NONE;
  }

  private static int findPrevPivot(List<Double> d, int start, int depth, boolean high) {
    return IntStream.iterate(start, idx -> idx > start - depth && idx > 0, idx -> idx - 1)
            .filter(idx -> high ? (d.get(idx) > d.get(idx-1) && d.get(idx) > d.get(idx+1))
                    : (d.get(idx) < d.get(idx-1) && d.get(idx) < d.get(idx+1)))
            .findFirst().orElse(-1);
  }
}
