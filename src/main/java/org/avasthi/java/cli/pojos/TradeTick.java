package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record TradeTick(UUID tradeId,
                        boolean tradable,
                        String symbol,
                        String name,
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
                        float openInterest,
                        float openInterestDayHigh,
                        float openInterestDayLow,
                        Date exchangeTimestamp) {

}
