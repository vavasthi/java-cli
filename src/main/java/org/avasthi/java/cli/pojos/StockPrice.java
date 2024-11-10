package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record StockPrice(UUID _id,
                         String symbol,
                         String series,
                         float open,
                         float high,
                         float low,
                         float close,
                         float last,
                         float prevClose,
                         long totalTransactedQuantity,
                         float totalTransactedValue,
                         Date timestamp,
                         int noTrades,
                         String isin) {
}
