package org.avasthi.java.cli.pojos;

public enum CpiGroup {

  CLOTHING_AND_FOOTWEAR("Clothing and Footwear"),
  FOOD_AND_BEVERAGES("Consumer Food Price"),
  FUEL_AND_LIGHT("Fuel and Light"),
  GENERAL("General"),
  HOUSING("Housing"),
  MISCELLANEOUS("Miscellaneous"),
  PAN_TOBACCO_AND_INTOXICANTS("Pan, Tobacco and Intoxicants");

  private final String group;

  CpiGroup(String group) {
    this.group = group;
  }

  public String getGroup() {
    return group;
  }
}
