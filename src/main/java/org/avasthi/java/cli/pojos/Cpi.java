package org.avasthi.java.cli.pojos;

import java.util.UUID;

public class Cpi {
  public Cpi() {
  }
  public Cpi(int month,
             int year,
             String state) {
    this.id = UUID.randomUUID();
    this.month = month;
    this.year = year;
    this.state = state;
  }

  public Cpi(UUID id,
             int month,
             int year,
             String state,
             float clothing,
             float clothingAndFootwearOverall,
             float footwear,
             float consumerFoodPriceOverall,
             float cerealsAndProducts,
             float egg,
             float foodAndBeveragesOverall,
             float fruits,
             float meatAndFish,
             float milkAndProducts,
             float nonAlcoholicBeverages,
             float oilsAndFats,
             float preparedMeals,
             float pulsesAndProducts,
             float spices,
             float sugerAndConfectionary,
             float vegetables,
             float fuelAndLightOverall,
             float generalOverall,
             float housingOverall,
             float education,
             float health,
             float householdGoodsAndServices,
             float miscellaneousOverall,
             float personalCareAndEffects,
             float recreationAndAmusement,
             float transportAndCommunicaation,
             float tobaccoAndIntoxicants) {

    Clothing = clothing;
    this.id = id;
    this.month = month;
    this.year = year;
    this.state = state;
    this.clothingAndFootwearOverall = clothingAndFootwearOverall;
    this.footwear = footwear;
    this.consumerFoodPriceOverall = consumerFoodPriceOverall;
    this.cerealsAndProducts = cerealsAndProducts;
    this.egg = egg;
    this.foodAndBeveragesOverall = foodAndBeveragesOverall;
    this.fruits = fruits;
    this.meatAndFish = meatAndFish;
    this.milkAndProducts = milkAndProducts;
    this.nonAlcoholicBeverages = nonAlcoholicBeverages;
    this.oilsAndFats = oilsAndFats;
    this.preparedMeals = preparedMeals;
    this.pulsesAndProducts = pulsesAndProducts;
    this.spices = spices;
    this.sugerAndConfectionary = sugerAndConfectionary;
    this.vegetables = vegetables;
    this.fuelAndLightOverall = fuelAndLightOverall;
    this.generalOverall = generalOverall;
    this.housingOverall = housingOverall;
    this.education = education;
    this.health = health;
    this.householdGoodsAndServices = householdGoodsAndServices;
    this.miscellaneousOverall = miscellaneousOverall;
    this.personalCareAndEffects = personalCareAndEffects;
    this.recreationAndAmusement = recreationAndAmusement;
    this.transportAndCommunicaation = transportAndCommunicaation;
    this.tobaccoAndIntoxicants = tobaccoAndIntoxicants;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public float getClothing() {
    return Clothing;
  }

  public void setClothing(float clothing) {
    Clothing = clothing;
  }

  public float getClothingAndFootwearOverall() {
    return clothingAndFootwearOverall;
  }

  public void setClothingAndFootwearOverall(float clothingAndFootwearOverall) {
    this.clothingAndFootwearOverall = clothingAndFootwearOverall;
  }

  public float getFootwear() {
    return footwear;
  }

  public void setFootwear(float footwear) {
    this.footwear = footwear;
  }

  public float getConsumerFoodPriceOverall() {
    return consumerFoodPriceOverall;
  }

  public void setConsumerFoodPriceOverall(float consumerFoodPriceOverall) {
    this.consumerFoodPriceOverall = consumerFoodPriceOverall;
  }

  public float getCerealsAndProducts() {
    return cerealsAndProducts;
  }

  public void setCerealsAndProducts(float cerealsAndProducts) {
    this.cerealsAndProducts = cerealsAndProducts;
  }

  public float getEgg() {
    return egg;
  }

  public void setEgg(float egg) {
    this.egg = egg;
  }

  public float getFoodAndBeveragesOverall() {
    return foodAndBeveragesOverall;
  }

  public void setFoodAndBeveragesOverall(float foodAndBeveragesOverall) {
    this.foodAndBeveragesOverall = foodAndBeveragesOverall;
  }

  public float getFruits() {
    return fruits;
  }

  public void setFruits(float fruits) {
    this.fruits = fruits;
  }

  public float getMeatAndFish() {
    return meatAndFish;
  }

  public void setMeatAndFish(float meatAndFish) {
    this.meatAndFish = meatAndFish;
  }

  public float getMilkAndProducts() {
    return milkAndProducts;
  }

  public void setMilkAndProducts(float milkAndProducts) {
    this.milkAndProducts = milkAndProducts;
  }

  public float getNonAlcoholicBeverages() {
    return nonAlcoholicBeverages;
  }

  public void setNonAlcoholicBeverages(float nonAlcoholicBeverages) {
    this.nonAlcoholicBeverages = nonAlcoholicBeverages;
  }

  public float getOilsAndFats() {
    return oilsAndFats;
  }

  public void setOilsAndFats(float oilsAndFats) {
    this.oilsAndFats = oilsAndFats;
  }

  public float getPreparedMeals() {
    return preparedMeals;
  }

  public void setPreparedMeals(float preparedMeals) {
    this.preparedMeals = preparedMeals;
  }

  public float getPulsesAndProducts() {
    return pulsesAndProducts;
  }

  public void setPulsesAndProducts(float pulsesAndProducts) {
    this.pulsesAndProducts = pulsesAndProducts;
  }

  public float getSpices() {
    return spices;
  }

  public void setSpices(float spices) {
    this.spices = spices;
  }

  public float getSugerAndConfectionary() {
    return sugerAndConfectionary;
  }

  public void setSugerAndConfectionary(float sugerAndConfectionary) {
    this.sugerAndConfectionary = sugerAndConfectionary;
  }

  public float getVegetables() {
    return vegetables;
  }

  public void setVegetables(float vegetables) {
    this.vegetables = vegetables;
  }

  public float getFuelAndLightOverall() {
    return fuelAndLightOverall;
  }

  public void setFuelAndLightOverall(float fuelAndLightOverall) {
    this.fuelAndLightOverall = fuelAndLightOverall;
  }

  public float getGeneralOverall() {
    return generalOverall;
  }

  public void setGeneralOverall(float generalOverall) {
    this.generalOverall = generalOverall;
  }

  public float getHousingOverall() {
    return housingOverall;
  }

  public void setHousingOverall(float housingOverall) {
    this.housingOverall = housingOverall;
  }

  public float getEducation() {
    return education;
  }

  public void setEducation(float education) {
    this.education = education;
  }

  public float getHealth() {
    return health;
  }

  public void setHealth(float health) {
    this.health = health;
  }

  public float getHouseholdGoodsAndServices() {
    return householdGoodsAndServices;
  }

  public void setHouseholdGoodsAndServices(float householdGoodsAndServices) {
    this.householdGoodsAndServices = householdGoodsAndServices;
  }

  public float getMiscellaneousOverall() {
    return miscellaneousOverall;
  }

  public void setMiscellaneousOverall(float miscellaneousOverall) {
    this.miscellaneousOverall = miscellaneousOverall;
  }

  public float getPersonalCareAndEffects() {
    return personalCareAndEffects;
  }

  public void setPersonalCareAndEffects(float personalCareAndEffects) {
    this.personalCareAndEffects = personalCareAndEffects;
  }

  public float getRecreationAndAmusement() {
    return recreationAndAmusement;
  }

  public void setRecreationAndAmusement(float recreationAndAmusement) {
    this.recreationAndAmusement = recreationAndAmusement;
  }

  public float getTransportAndCommunicaation() {
    return transportAndCommunicaation;
  }

  public void setTransportAndCommunicaation(float transportAndCommunicaation) {
    this.transportAndCommunicaation = transportAndCommunicaation;
  }

  public float getTobaccoAndIntoxicants() {
    return tobaccoAndIntoxicants;
  }

  public void setTobaccoAndIntoxicants(float tobaccoAndIntoxicants) {
    this.tobaccoAndIntoxicants = tobaccoAndIntoxicants;
  }

  private UUID id;
  private int month;
  private int year;
  private String state;
  private float Clothing;
  private float clothingAndFootwearOverall;
  private float footwear;
  private float consumerFoodPriceOverall;
  private float cerealsAndProducts;
  private float egg;
  private float foodAndBeveragesOverall;
  private float fruits;
  private float meatAndFish;
  private float milkAndProducts;
  private float nonAlcoholicBeverages;
  private float oilsAndFats;
  private float preparedMeals;
  private float pulsesAndProducts;
  private float spices;
  private float sugerAndConfectionary;
  private float vegetables;
  private float fuelAndLightOverall;
  private float generalOverall;
  private float housingOverall;
  private float education;
  private float health;
  private float householdGoodsAndServices;
  private float miscellaneousOverall;
  private float personalCareAndEffects;
  private float recreationAndAmusement;
  private float transportAndCommunicaation;
  private float tobaccoAndIntoxicants;
}
