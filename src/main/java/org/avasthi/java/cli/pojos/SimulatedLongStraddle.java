package org.avasthi.java.cli.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedLongStraddle {

  public record Straddle(SimulatedTrade call, SimulatedTrade put) {

  }
  private ObjectId id;
  private UUID tradeId;
  private Date timestamp;
  private String asset;
  private long strike;
  private Straddle buy;
  private Straddle sell;
  private double maxProfit;
  private long profitOpportunityCount;
  private List<Straddle> otherSellOpportunities;
}
