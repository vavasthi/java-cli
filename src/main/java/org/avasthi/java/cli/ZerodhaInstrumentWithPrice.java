package org.avasthi.java.cli;

import lombok.Builder;
import lombok.Data;
import org.avasthi.java.cli.pojos.TradeTick;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ZerodhaInstrumentWithPrice {
    private ZerodhaInstrument zerodhaInstrument;
    private TradeTick initialTick;
    public float updateTradeTick(TradeTick tradeTick) {
        if (initialTick == null) {
          initialTick = tradeTick;
        }
        return (tradeTick.lastPrice() - initialTick.lastPrice()) * zerodhaInstrument.lotSize();
    }
    public float getCost() {
      return initialTick.lastPrice() * zerodhaInstrument.lotSize();
    }
}
