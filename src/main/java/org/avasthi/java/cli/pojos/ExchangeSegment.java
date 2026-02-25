package org.avasthi.java.cli.pojos;

public enum ExchangeSegment {

  BSE_FUTURES(Exchange.BSE, Segment.FUTURES, "BFO-FUT"),
  BSE_OPTIONS(Exchange.BSE, Segment.OPTIONS, "BFO-OPT"),
  BSE_INDICES(Exchange.BSE, Segment.INDICES, "INDICES"),
  BSE_EQUITY(Exchange.BSE, Segment.EQUITY, "BSE"),
  CDS_FUTURES(Exchange.CDS, Segment.FUTURES, "CDS-FUT"),
  CDS_OPTIONS(Exchange.CDS, Segment.OPTIONS, "CDS-OPT"),
  CDS_INDICES(Exchange.CDS, Segment.INDICES, "INDICES"),
  NSE_FUTURES(Exchange.NSE, Segment.FUTURES, "NFO-FUT"),
  NSE_OPTIONS(Exchange.NSE, Segment.OPTIONS, "NFO-OPT"),
  NSE_INDICES(Exchange.NSE, Segment.INDICES, "INDICES"),
  NSE_EQUITY(Exchange.NSE, Segment.EQUITY, "NSE"),
  NSE_COMMODITY(Exchange.NSE, Segment.CO, "NCO"),
  NSE_COMMODITY_FUTURES(Exchange.NSE, Segment.CO_FUTURES, "NCO-FUT"),
  NSE_COMMODITY_OPTIONS(Exchange.NSE, Segment.CO_OPTIONS, "NCO-OPT"),
  MCX_FUTURES(Exchange.MCX, Segment.FUTURES, "MCX-FUT"),
  MCX_OPTIONS(Exchange.MCX, Segment.OPTIONS, "MCX-OPT"),
  MCX_INDICES(Exchange.MCX, Segment.INDICES, "INDICES"),
  GLOBAL_INDICES(Exchange.GLOBAL, Segment.INDICES, "INDICES");

  private final Exchange exchange;
  private final Segment segment;
  private final String value;


  ExchangeSegment(Exchange exchange, Segment segment, String value) {
    this.value = value;
    this.exchange = exchange;
    this.segment = segment;
  }

  public Exchange getExchange() {
    return exchange;
  }

  public Segment getSegment() {
    return segment;
  }

  public String getValue() {
    return value;
  }
  public static ExchangeSegment create(String exchange, String segment) {
    Exchange e = Exchange.create(exchange);
    if (segment.equals("INDICES")) {
      return switch (e) {
        case BSE ->  BSE_INDICES;
        case NSE -> NSE_INDICES;
        case GLOBAL -> GLOBAL_INDICES;
        case CDS -> CDS_INDICES;
        case MCX -> MCX_INDICES;
      };
    }
    else {
      for (ExchangeSegment es : values()) {
        if (es.exchange.equals(e) && es.value.equals(segment)) {
          return es;
        }
      }
    }
    throw new RuntimeException(String.format("Invalid combination of exchange %s and segment %s", exchange, segment));
  }
}
