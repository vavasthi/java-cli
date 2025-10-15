package org.avasthi.java.cli.pojos;

public enum IipCategory {

  BASIC_GOODS("Basic Goods"),
  MINING("Mining"),
  MANUFACTURING("Manufacturing"),
  ELECTRICITY("Electricity"),
  GENERAL("General"),
  PRIMARY_GOODS("Primary goods"),
  CAPITAL_GOODS("Capital goods"),
  INTERMEDIATE_GOODS("Intermediate goods"),
  INFRASTRUCTURE_CONSTRUCTION_GOODS("Infrastructure/ construction goods"),
  CONSUMER_DURABLES("Consumer durables"),
  CONSUMER_NON_DURABLES("Consumer non-durables");

  private final String category;

  IipCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return category;
  }
}
