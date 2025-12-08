package org.avasthi.java.cli.pojos;

public enum DerivativeType {
  IndexFutures("FUTIDX", "IDF"),
  StockFutures("FUTSTK", "STF"),
  IndexOptions("OPTIDX", "IDO"),
  StockOptions("OPTSTK", "STO"),
  SomethingDifferent("", "");
  DerivativeType(String oldCode, String newCode) {
    this.oldCode = oldCode;
    this.newCode = newCode;
  }
  private final String oldCode;
  private final String newCode;
  public static DerivativeType getFromOldCode(String oldCode) {
    for (DerivativeType ot :values()) {
      if (ot.oldCode.equals(oldCode)) {
        return ot;
      }
    }
    return SomethingDifferent;
  }
  public static DerivativeType getFromNewCode(String newCode) {
    for (DerivativeType ot :values()) {
      if (ot.newCode.equals(newCode)) {
        return ot;
      }
    }
    return SomethingDifferent;
  }
}
