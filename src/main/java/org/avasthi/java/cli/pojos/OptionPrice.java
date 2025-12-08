package org.avasthi.java.cli.pojos;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

public record OptionPrice(UUID _id,
                          DerivativeType derivativeType,
                          OptionType optionType,
                          String symbol,
                          Date expiryDate,
                          float strikePrice,
                          float open,
                          float high,
                          float low,
                          float close,
                          float settlePrice,
                          float contracts,
                          float value,
                          float openInterest,
                          float changeInOpenInterest,
                          Date date,
                          String isin) {
}
