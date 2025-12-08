package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record StockPrice(UUID _id,
                         String symbol,
                         Date date,
                         String series,
                         float open,
                         float high,
                         float low,
                         float close,
                         float last,
                         float prevClose,
                         float adjustedClose,
                         float transactedQuantity,
                         float volume,
                         String isin) {
}
