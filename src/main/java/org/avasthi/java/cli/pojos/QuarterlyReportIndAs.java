package org.avasthi.java.cli.pojos;

public enum QuarterlyReportIndAs {
  IND_AS_NEW("Ind-AS New"),
  NON_IND_AS("Non-Ind-AS"),
  NBFC_IND("NBFC-IND"),
  IND_AS("Ind-AS");

  private final String description;
  QuarterlyReportIndAs(String  description) {
    this.description = description;
  }
  public static QuarterlyReportIndAs  fromDescription(String description) {
    for (QuarterlyReportIndAs qria : values()) {
      if (qria.description.equals(description)) {
        return qria;
      }
    }
    throw new IllegalArgumentException(String.format("The value of description is " + description));
  }
}
