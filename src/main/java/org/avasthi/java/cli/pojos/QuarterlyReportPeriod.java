package org.avasthi.java.cli.pojos;

public enum QuarterlyReportPeriod {
  QUARTERLY("Quarterly");

  private final String description;
  QuarterlyReportPeriod(String  description) {
    this.description = description;
  }
  public static QuarterlyReportPeriod fromDescription(String description) {
    for (QuarterlyReportPeriod p : values()) {
      if (p.description.equals(description)) {
        return p;
      }
    }
    throw new IllegalArgumentException();
  }
}
