package org.avasthi.java.cli.pojos;

public enum Exchange {

  BSE,
  NSE,
  CDS,
  GLOBAL,
  MCX;

  public static Exchange create(String value) {
    if (value.equals("BFO")) {
      return BSE;
    }
    else if (value.equals("NFO")) {
      return NSE;
    }
    for (Exchange e : values()) {
      if (e.name().equals(value)) {
        return e;
      }
    }
    throw new RuntimeException(String.format("Invalid exchange value %s", value));
  }
}
