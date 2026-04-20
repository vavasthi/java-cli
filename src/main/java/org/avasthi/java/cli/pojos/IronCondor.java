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
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IronCondor {

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
  public record TradeTicks(TradeTick lowerPutBuyTradeTick,
                           TradeTick lowerPutSellTradeTick,
                           TradeTick upperCallSellTradeTick,
                           TradeTick upperCallBuyTradeTick,
                           TradeTick spotTradeTick,
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
  private ZerodhaInstrumentWithPrice lowerPutBuy;
  private ZerodhaInstrumentWithPrice lowerPutSell;
  private ZerodhaInstrumentWithPrice upperCallSell;
  private ZerodhaInstrumentWithPrice upperCallBuy;
  private final List<TradeTicks> otherTicks = new ArrayList<>();
  public  STATUS updateTicks(TradeTick lowerPutBuyTradeTick,
                          TradeTick lowerPutSellTradeTick,
                          TradeTick upperCallSellTradeTick,
                          TradeTick upperCallBuyTradeTick,
                          TradeTick spotTradeTick,
                          float targetProfitPercentage,
                          float targetLossPercentage) {
    lowerPutBuy.updateTradeTick(lowerPutBuyTradeTick);
    lowerPutSell.updateTradeTick(lowerPutSellTradeTick);
    upperCallSell.updateTradeTick(upperCallSellTradeTick);
    upperCallBuy.updateTradeTick(upperCallBuyTradeTick);
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
    float cost = lowerPutBuy.getCost() - lowerPutSell.getCost() - upperCallSell.getCost() + upperCallBuy.getCost();
    cost = 100000;
    status = STATUS.RUNNING;
    if ((profit / cost) > targetProfitPercentage) {
      status = STATUS.TARGET_PROFIT_REACHED;
    }
    else if (profit < 0 && (-profit / cost) > targetLossPercentage ) {
      status = STATUS.TARGET_STOP_LOSS_REACHED;
    }
    otherTicks.add(new TradeTicks(lowerPutBuyTradeTick, lowerPutSellTradeTick, upperCallSellTradeTick, upperCallBuyTradeTick, spotTradeTick, status));
    return status;
  }
  public boolean isValid() {
    return lowerPutBuy != null && lowerPutSell != null && upperCallSell != null && upperCallBuy != null;
  }
  public float getprofit() {
    return profit;
  }
  public float computeProfit(TradeTicks tt) {

    TradeTicks firstTick = otherTicks.getFirst();
    float profit = (tt.lowerPutBuyTradeTick.lastPrice()  - firstTick.lowerPutBuyTradeTick.lastPrice())*lowerPutBuy.getZerodhaInstrument().lotSize();
    profit += (tt.upperCallBuyTradeTick.lastPrice()  - firstTick.upperCallBuyTradeTick.lastPrice())*upperCallBuy.getZerodhaInstrument().lotSize();
    profit += (firstTick.lowerPutSellTradeTick.lastPrice()  - tt.lowerPutSellTradeTick.lastPrice())*lowerPutSell.getZerodhaInstrument().lotSize();
    profit += (firstTick.upperCallSellTradeTick.lastPrice()  - tt.upperCallSellTradeTick.lastPrice())*upperCallSell.getZerodhaInstrument().lotSize();
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

  public ZerodhaInstrumentWithPrice getLowerPutBuy() {
    return lowerPutBuy;
  }

  public ZerodhaInstrumentWithPrice getLowerPutSell() {
    return lowerPutSell;
  }

  public ZerodhaInstrumentWithPrice getUpperCallSell() {
    return upperCallSell;
  }

  public ZerodhaInstrumentWithPrice getUpperCallBuy() {
    return upperCallBuy;
  }

  public List<TradeTicks> getOtherTicks() {
    return otherTicks;
  }
}
