package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;


public record IndexPrice(UUID id, Date date, String name, IndexSymbols symbol, float open, float high, float close, float low, float turnover, long tradedQuantity) {

}
