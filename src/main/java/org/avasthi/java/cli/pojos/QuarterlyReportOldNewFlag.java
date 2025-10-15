package org.avasthi.java.cli.pojos;

public enum QuarterlyReportOldNewFlag {
  OLD("O"),
  NEW("N"),
  UNKNOWN("U"),;

  private final String description;
  QuarterlyReportOldNewFlag(String  description) {
    this.description = description;
  }
  public static QuarterlyReportOldNewFlag fromDescription(String description) {
    for (QuarterlyReportOldNewFlag qria : values()) {
      if (qria.description.equals(description)) {
        return qria;
      }
    }
    throw new IllegalArgumentException();
  }
}
