package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record TradeTick(UUID id,
                        boolean tradable,
                        String symbol,
                        float lastPrice,
                        float lastTradedQuantity,
                        long volumeTraded,
                        long buyQuantity,
                        long sellQuantity,
                        float open,
                        float high,
                        float low,
                        float close,
                        float change,
                        Date timestamp,
                        float openInterest,
                        float openInterestHigh,
                        float openInterestLow,
                        Date exchangeTimestamp) {

}
