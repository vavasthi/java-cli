package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record ZerodhaInstruments(String instrumentToken,
                                 String exchangeToken,
                                 String symbol,
                                 String name,
                                 Date expiry,
                                 long strike,
                                 float tick_size,
                                 long lotSize,
                                 String instrumentType,
                                 ExchangeSegment es ) {
}
