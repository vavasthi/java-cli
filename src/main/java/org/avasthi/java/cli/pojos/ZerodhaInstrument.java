package org.avasthi.java.cli.pojos;

import java.util.Date;

public record ZerodhaInstrument(String instrumentToken,
                                String exchangeToken,
                                String symbol,
                                String name,
                                Date expiry,
                                float strike,
                                float tick_size,
                                long lotSize,
                                String instrumentType,
                                ExchangeSegment es ) {
}
