package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

record SingleDepthRecord(float quantity, float price, long orders) {

}
public record TradeTickDepth(UUID id, String symbol, Date timestmap, Date exchangeTimestamp, Map<String, List<SingleDepthRecord>> depth) {

}
