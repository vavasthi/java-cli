package org.avasthi.java.cli.pojos;

public enum OptionType {
  Call("CE", "CE"),
  Put("PE", "PE"),
  NOT_OPTION("XX", "");
  OptionType(String oldCode, String newCode) {
    this.oldCode = oldCode;
    this.newCode = newCode;
  }
  private final String oldCode;
  private final String newCode;
  public static OptionType getFromOldCode(String oldCode) {
    for (OptionType ot :values()) {
      if (ot.oldCode.equals(oldCode)) {
        return ot;
      }
    }
    return NOT_OPTION;
  }
  public static OptionType getFromNewCode(String newCode) {
    for (OptionType ot :values()) {
      if (ot.newCode.equals(newCode)) {
        return ot;
      }
    }
    return NOT_OPTION;
  }
}
