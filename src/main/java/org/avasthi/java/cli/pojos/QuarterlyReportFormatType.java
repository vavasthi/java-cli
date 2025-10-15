package org.avasthi.java.cli.pojos;

public enum QuarterlyReportFormatType {
  NEW("New"),
  OLD("Old");
  private final String description;
  QuarterlyReportFormatType(String  description) {
    this.description = description;
  }
  public static QuarterlyReportFormatType  fromDescription(String description) {
    for (QuarterlyReportFormatType ft : values()) {
      if (ft.description.equals(description)) {
        return ft;
      }
    }
    throw new IllegalArgumentException(String.format("The value of description is " + description));
  }
}
