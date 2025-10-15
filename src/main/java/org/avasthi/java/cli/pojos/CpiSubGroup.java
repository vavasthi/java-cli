package org.avasthi.java.cli.pojos;

public enum CpiSubGroup {

  CEREALS_AND_PRODUCTS("Cereals and Products"),
  CLOTHING("Clothing"),
  CLOTHING_AND_FOOTWEAR_OVERALL("Clothing and Footwear-Overall"),
  CONSUMER_FOOD_PRICE_OVERALL("Consumer Food Price-Overall"),
  EDUCATION("Education"),
  EGG("Egg"),
  FOOD_AND_BEVERAGES_OVERALL("Food and Beverages-Overall"),
  FOOTWEAR("Footwear"),
  FRUITS("Fruits"),
  FUEL_AND_LIGHT_OVERALL("Fuel and Light-Overall"),
  GENERAL_OVERALL("General-Overall"),
  HEALTH("Health"),
  HOUSEHOLD_GOODS_AND_SERVICES("Household Goods and Services"),
  HOUSING_OVERALL("Housing-Overall"),
  MEAT_AND_FISH("Meat and Fish"),
  MILK_AND_PRODUCTS("Milk and Products"),
  MISCELLANEOUS_OVERALL("Miscellaneous-Overall"),
  NON_ALCOHOLIC_BEVERAGES("Non-alcoholic Beverages"),
  OILS_AND_FATS("Oils and Fats"),
  PERSONAL_CARE_AND_EFFECTS("Personal Care and Effects"),
  PREPARED_MEALS("Prepared Meals, Snacks, Sweets etc."),
  PULSES_AND_PRODUCTS("Pulses and Products"),
  RECREATION_AND_AMUSEMENT("Recreation and Amusement"),
  SPICES("Spices"),
  SUGAR_AND_CONFECTIONERY("Sugar and Confectionery"),
  PAN_TOBACCO_AND_INTOXICANTS_OVERALL("Pan, Tobacco and Intoxicants-Overall"),
  TRANSPORT_AND_COMMUNICATION("Transport and Communication"),
  VEGETABLES("Vegetables");

  private final String subGroup;

  CpiSubGroup(String subGroup) {
    this.subGroup = subGroup;
  }

  public String getSubGroup() {
    return subGroup;
  }
}
