package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record Bhav(UUID _id,
                   String symbol,
                   String series,
                   Date date,
                   float open,
                   float high,
                   float low,
                   float close,
                   float last,
                   float prevClose,
                   long totalTransactedQuantity,
                   float totalTransactedValue,
                   Long totalTrades,
                   String isin,
                   String filename) {
}
