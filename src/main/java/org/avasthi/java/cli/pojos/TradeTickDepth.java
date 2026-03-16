package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TradeTickDepth(UUID tradeId, String symbol, String name, Date exchangeTimestamp, Map<String, List<SingleDepthRecord>> depth) {

}
