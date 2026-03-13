package org.avasthi.java.cli;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.YEAR;

public class ImpliedVolatility {

    // Standard Normal Cumulative Distribution Function (CDF)
    private static double Math_erf(double x) {
        // Simple approximation for the CDF of the normal distribution
        double t = 1.0 / (1.0 + 0.5 * Math.abs(x));
        double ans = 1 - t * Math.exp(-x * x - 1.26551223 +
                t * (1.00002368 + t * (0.37409196 + t * (0.09678418 +
                        t * (-0.18628806 + t * (0.27886807 + t * (-1.13520398 +
                                t * (1.48851587 + t * (-0.82215223 + t * (0.17087277))))))))));
        return x >= 0 ? ans : -ans;
    }

    private static double normalCDF(double x) {
        return 0.5 * (1.0 + Math_erf(x / Math.sqrt(2.0)));
    }

    private static double normalPDF(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2 * Math.PI);
    }

    // Black-Scholes Pricing Function
    public static double blackScholesPrice(double spotPrice,
                                           double strikePrice,
                                           double timeToExpiryInYears,
                                           double riskFreeRate,
                                           double impliedVolatility,
                                           boolean isCall) {
        double d1 = (Math.log(spotPrice / strikePrice) + (riskFreeRate + 0.5 * impliedVolatility * impliedVolatility) * timeToExpiryInYears) / (impliedVolatility * Math.sqrt(timeToExpiryInYears));
        double d2 = d1 - impliedVolatility * Math.sqrt(timeToExpiryInYears);

        if (isCall) {
            return spotPrice * normalCDF(d1) - strikePrice * Math.exp(-riskFreeRate * timeToExpiryInYears) * normalCDF(d2);
        } else {
            return strikePrice * Math.exp(-riskFreeRate * timeToExpiryInYears) * normalCDF(-d2) - spotPrice * normalCDF(-d1);
        }
    }

    // Vega Calculation (The derivative of price with respect to impliedVolatility)
    public static double calculateVega(double spotPrice,
                                       double strikePrice,
                                       double timeToExpiryInYears,
                                       double riskFreeRate,
                                       double impliedVolatility) {
        double d1 = (Math.log(spotPrice / strikePrice) + (riskFreeRate + 0.5 * impliedVolatility * impliedVolatility) * timeToExpiryInYears) / (impliedVolatility * Math.sqrt(timeToExpiryInYears));
        return spotPrice * Math.sqrt(timeToExpiryInYears) * normalPDF(d1);
    }

    public static double putPriceFromCall(double callPrice,
                                          double spotPrice,
                                          double strikePrice,
                                          double timeToExpiryInYears,
                                          double riskFreeRate) {

        double pv = strikePrice * Math.exp(-riskFreeRate * timeToExpiryInYears);
        return callPrice + pv - spotPrice;
    }
    public static double callPriceFromPut(double putPrice,
                                          double spotPrice,
                                          double strikePrice,
                                          double timeToExpiryInYears,
                                          double riskFreeRate) {
        double pv = strikePrice * Math.exp(-riskFreeRate * timeToExpiryInYears);
        return putPrice + spotPrice - pv;
    }
    /**
     * Calculates Implied Volatility using the Newton-Raphson method
     * @param targetPrice The current market price of the option
     * @param spotPrice Current stock price
     * @param strikePrice Strike price
     * @param timeToExpirationInYears Time to expiration (in years)
     * @param riskFreeRate Risk-free interest rate (e.g., 0.05 for 5%)
     * @param isCall True for call, False for put
     */
    public static double calculateIV(double targetPrice,
                                     double spotPrice,
                                     double strikePrice,
                                     double timeToExpirationInYears,
                                     double riskFreeRate,
                                     boolean isCall) {

        double impliedVolatility = 0.5; // Initial guess for volatility
        double epsilon = 1e-6; // Accuracy tolerance
        int maxIterations = 1000;

        for (int i = 0; i < maxIterations; i++) {
            double price = blackScholesPrice(spotPrice, strikePrice, timeToExpirationInYears, riskFreeRate, impliedVolatility, isCall);
            double vega = calculateVega(spotPrice, strikePrice, timeToExpirationInYears, riskFreeRate, impliedVolatility);

            // Avoid division by zero
            if (Math.abs(vega) < 1e-10) break;

            double diff = targetPrice - price;

            // Check if we are close enough to the target price
            if (Math.abs(diff) < epsilon) {
                return impliedVolatility;
            }

            // Newton-Raphson step: sigma_new = sigma_old + (TargetPrice - ModelPrice) / Vega
            impliedVolatility = impliedVolatility + (diff / vega);

            // Boundary checks: Volatility can't be negative or realistically above 500%
            if (impliedVolatility <= 0) impliedVolatility = 0.0001;
            if (impliedVolatility > 5.0) impliedVolatility = 5.0;
        }

        return impliedVolatility; // Return the best approximation found
    }

    private static double getTimeToExpiryInYears(Date expiryDate) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expiryDate);
        calendar.set(calendar.get(YEAR), 0, 1, 0 ,0, 0);
        LocalDateTime expiryDay = LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime startOfTheYear = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
        calendar.add(YEAR, 1);
        calendar.add(Calendar.SECOND, -1);
        LocalDateTime endOfTheYear = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());

        long secondsToExpiry = ChronoUnit.SECONDS.between(LocalDateTime.now(ZoneId.systemDefault()), expiryDay);
        long secondsInAYear = ChronoUnit.SECONDS.between(startOfTheYear, endOfTheYear);
        return (double)secondsToExpiry  / (double) secondsInAYear;

    }
    public static void main(String[] args) {
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.set(2026, 2, 17, 15, 30, 0);
        double targetPrice = 304;
        double spotPrice = 23916.65;
        double strikePrice = 23900;
        double timeToExpiry =  getTimeToExpiryInYears(expiryDate.getTime());
        double riskFreeRate = 0.07;
        System.out.println(timeToExpiry);

        double iv = calculateIV(targetPrice, spotPrice, strikePrice, timeToExpiry, riskFreeRate, true);
        System.out.printf("The Implied Volatility is: %.2f%%%n", iv * 100);
      //  iv = .15;

        double callPrice = blackScholesPrice(spotPrice, strikePrice, timeToExpiry, riskFreeRate, iv, true);
        double putPrice = blackScholesPrice(spotPrice, strikePrice, timeToExpiry, riskFreeRate, iv, false);
        double callPriceFromPut = callPriceFromPut(putPrice, spotPrice, strikePrice, timeToExpiry, riskFreeRate);
        double putPriceFromCall = putPriceFromCall(callPrice, spotPrice, strikePrice, timeToExpiry, riskFreeRate);
        System.out.printf("Call price is %f, put price is %f%n", callPrice, putPrice);
        System.out.printf("Call price from put is %f, put price from call is %f%n", callPriceFromPut, putPriceFromCall);
    }
}