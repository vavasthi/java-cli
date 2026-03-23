package org.avasthi.java.cli.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LongStraddle {

  public double getCost() {
    return buy.call().getPremium() * buy.call().getQuantity() + buy.put().getPremium() * buy.put().getQuantity();
  }

  public record Straddle(Trade call, Trade put) {

  }
  private ObjectId id;
  private UUID tradeId;
  private Date timestamp;
  private String asset;
  private float strike;
  private double spotPrice;
  private Straddle buy;
  private Straddle sell;
  private double maxProfit;
  private double maxLoss;
  private double vix;
  private long profitOpportunityCount;
  private List<Straddle> otherSellOpportunities;
  private double currentProfit;
}
