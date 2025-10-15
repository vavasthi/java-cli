package org.avasthi.java.cli.pojos;

public enum IIPRanges {
  BY_1993_94("1993-94", 1994, 2004),
  BY_2004_05("2004-05", 2005, 2011),
  BY_2011_12("2011-12", 2012, 2030);

  private final String baseYear;
  private final int startYear;
  private final int endYear;

  public String getBaseYear() {
    return baseYear;
  }

  public int getStartYear() {
    return startYear;
  }

  public int getEndYear() {
    return endYear;
  }

  IIPRanges(String baseYear, int startYear, int endYear) {
    this.baseYear = baseYear;
    this.startYear = startYear;
    this.endYear = endYear;
  }
}
