package org.avasthi.java.cli;

import java.util.ArrayList;
import java.util.List;

public class ImpulseMACD {

    private List<Double> macdLine = new ArrayList<>();
    private List<Double> signalLine = new ArrayList<>();
    private List<Double> histogram = new ArrayList<>();

    public enum Signal { BUY, SELL, HOLD }

    public void calculate(List<Double> prices, int filterPeriod, int fastPeriod, int slowPeriod, int signalPeriod) {
        int n = prices.size();
        if (n < slowPeriod) return;

        // Reset lists for fresh calculation
        macdLine = new ArrayList<>();
        signalLine = new ArrayList<>();
        histogram = new ArrayList<>();

        // 1. Calculate 'Value' (The Impulse Filter: Price - SMA of Price)
        List<Double> filteredSource = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double sma = getSMA(prices, i, filterPeriod);
            filteredSource.add(prices.get(i) - sma);
        }

        // 2. Calculate MACD Line (Fast SMA - Slow SMA of the Filtered Source)
        for (int i = 0; i < n; i++) {
            double f = getSMA(filteredSource, i, fastPeriod);
            double s = getSMA(filteredSource, i, slowPeriod);
            macdLine.add(f - s);
        }

        // 3. Calculate Signal Line (SMA of the MACD Line)
        for (int i = 0; i < n; i++) {
            signalLine.add(getSMA(macdLine, i, signalPeriod));
        }

        // 4. Calculate Histogram (MACD - Signal)
        for (int i = 0; i < n; i++) {
            histogram.add(macdLine.get(i) - signalLine.get(i));
        }
    }

    /**
     * Midline Crossover Logic
     * BUY: Histogram crosses above 0
     * SELL: Histogram crosses below 0
     */
    public Signal getMidlineSignal(int i) {
        if (i < 1 || i >= histogram.size()) return Signal.HOLD;

        double currentHisto = histogram.get(i);
        double prevHisto = histogram.get(i - 1);

        if (prevHisto < 0 && currentHisto >= 0) return Signal.BUY;
        if (prevHisto > 0 && currentHisto <= 0) return Signal.SELL;

        return Signal.HOLD;
    }

    private double getSMA(List<Double> data, int currentIndex, int period) {
        if (currentIndex < period - 1 || data.size() <= currentIndex) return 0.0;
        double sum = 0;
        for (int i = currentIndex; i > currentIndex - period; i--) {
            sum += data.get(i);
        }
        return sum / period;
    }

    // Getters for integration
    public double getHistogram(int i) { return histogram.get(i); }
    public double getMacdLine(int i) { return macdLine.get(i); }
    public double getSignalLine(int i) { return signalLine.get(i); }
}