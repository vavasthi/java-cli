package org.avasthi.java.cli.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

  public enum TradeType {
    BUY,
    SELL
  }
  private Date timestamp;
  private TradeType tradeType;
  private String symbol;
  private long quantity;
  private double premium;
  private double IV;
  private double openInterestHigh;
  private double openInterestLow;
  private double volumeTraded;
  private double spotPrice;
}
