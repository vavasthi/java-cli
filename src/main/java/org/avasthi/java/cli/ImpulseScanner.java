package org.avasthi.java.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImpulseScanner {

    public static void main(String[] args) {
        // 1. Mock Data: In a real app, you'd load this from a CSV or API
        Map<String, List<Double>> marketData = loadMarketData();

        // 2. Initialize the Indicator
        ImpulseMACD indicator = new ImpulseMACD();

        System.out.println("=== IMPULSE MACD SCANNER REPORT ===");
        System.out.println("Ticker\t\tSignal\t\tHistogramValue");
        System.out.println("----------------------------------------------");

        for (String ticker : marketData.keySet()) {
            List<Double> prices = marketData.get(ticker);

            // Standard Impulse Settings: 34 (Filter), 34 (Fast), 125 (Slow), 9 (Signal)
            indicator.calculate(prices, 34, 34, 125, 9);

            int lastIdx = prices.size() - 1;
            ImpulseMACD.Signal signal = indicator.getMidlineSignal(lastIdx);
            double histoValue = indicator.getHistogram(lastIdx);

            // Only report if there is an active crossover signal
            if (signal != ImpulseMACD.Signal.HOLD) {
                System.out.printf("%s\t\t%s\t\t%.4f%n", ticker, signal, histoValue);
            }
        }
    }

    /**
     * Helper to simulate multiple stocks.
     * Replace this with your actual data ingestion logic.
     */
    private static Map<String, List<Double>> loadMarketData() {
        Map<String, List<Double>> data = new HashMap<>();

        // Let's mock 3 different tickers
        data.put("AAPL", generateMockPrices(150.0, 0.05));
        data.put("BTC/USD", generateMockPrices(60000.0, 0.15));
        data.put("TSLA", generateMockPrices(200.0, -0.02));

        return data;
    }

    private static List<Double> generateMockPrices(double start, double trend) {
        List<Double> prices = new ArrayList<>();
        double current = start;
        for (int i = 0; i < 200; i++) {
            current += (current * trend / 100) + (Math.random() * 2 - 1);
            prices.add(current);
        }
        return prices;
    }
}