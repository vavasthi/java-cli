package org.avasthi.java.cli.pojos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.avasthi.java.cli.ZerodhaInstrumentWithPrice;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionSellSpread {

  public enum STATUS {
    TARGET_PROFIT_REACHED("P"),
    TARGET_STOP_LOSS_REACHED("STPL"),
    RUNNING("R");

    private final String status;
    STATUS(String status) {
      this.status = status;
    }
    public String getStatus() {
      return status;
    }
  }
  public record TradeTicks(TradeTick putSell,
                           TradeTick callSell,
                           TradeTick spot,
                           STATUS status) {
  }
  private ObjectId id;
  private Date timestamp;
  private String asset;
  private double spotPrice;
  private float maxProfit;
  private float maxLoss;
  private float profit;
  private STATUS status;
  private ZerodhaInstrumentWithPrice putSell;
  private ZerodhaInstrumentWithPrice callSell;
  private final List<TradeTicks> otherTicks = new ArrayList<>();
  public  STATUS updateTicks(TradeTick putSell,
                          TradeTick callSell,
                          TradeTick spotTradeTick,
                          float targetProfitPercentage,
                          float targetLossPercentage) {
    this.putSell.updateTradeTick(putSell);
    this.callSell.updateTradeTick(callSell);
    float profit = 0;
    if (otherTicks.size() > 0) {

      profit = computeProfit(otherTicks.getLast());
    }
    if (profit < 0 && maxLoss < -profit) {
      maxLoss = - profit;
    }
    else if (profit > 0 & maxProfit < profit) {
      maxProfit = profit;
    }
    this.profit = profit;
    float premium = this.putSell.getCost() + this.callSell.getCost();
    status = STATUS.RUNNING;
    if ((profit / premium) > targetProfitPercentage) {
      status = STATUS.TARGET_PROFIT_REACHED;
    }
    else if (profit < 0 && (-profit / premium) > targetLossPercentage ) {
      status = STATUS.TARGET_STOP_LOSS_REACHED;
    }
    otherTicks.add(new TradeTicks(putSell, callSell, spotTradeTick, status));
    return status;
  }
  public boolean isValid() {
    return putSell != null && callSell != null;
  }
  public float computeProfit(TradeTicks tt) {

    TradeTicks firstTick = otherTicks.getFirst();
    float profit = (firstTick.putSell().lastPrice() - tt.putSell().lastPrice() + firstTick.callSell.lastPrice() - tt.callSell.lastPrice())*putSell.getZerodhaInstrument().lotSize();
    return profit;
  }

  public ObjectId getId() {
    return id;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getAsset() {
    return asset;
  }

  public double getSpotPrice() {
    return spotPrice;
  }

  public float getMaxProfit() {
    return maxProfit;
  }

  public float getMaxLoss() {
    return maxLoss;
  }

  public float getProfit() {
    return profit;
  }

  public STATUS getStatus() {
    return status;
  }

  public ZerodhaInstrumentWithPrice getPutSell() {
    return putSell;
  }

  public ZerodhaInstrumentWithPrice getCallSell() {
    return callSell;
  }

  public List<TradeTicks> getOtherTicks() {
    return otherTicks;
  }
}
