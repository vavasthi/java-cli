package org.avasthi.java.cli.pojos;

public enum IndexSymbols {
  NIFTY("S&P CNX NIFTY"),
  UNKNOWN("Unknown");

  IndexSymbols(String fullname) {
    this.fullname = fullname;
  }

  private final String fullname;
  public static IndexSymbols getFromFullname(String fullname) {
    for (IndexSymbols is : values()) {
      if (is.fullname.equals(fullname)) {
        return is;
      }
    }
    return UNKNOWN;
  }
}
