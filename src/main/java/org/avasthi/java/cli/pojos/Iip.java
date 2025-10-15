package org.avasthi.java.cli.pojos;

import java.util.UUID;

public class Iip {
  public Iip() {
  }
  public Iip(int month,
             int year,
             int baseYear) {
    this.id = UUID.randomUUID();
    this.month = month;
    this.year = year;
    this.baseYear = baseYear;
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

  public int getBaseYear() {
    return baseYear;
  }

  public void setBaseYear(int baseYear) {
    this.baseYear = baseYear;
  }

  public float getMining() {
    return mining;
  }

  public void setMining(float mining) {
    this.mining = mining;
  }

  public float getConsumerNonDurables() {
    return consumerNonDurables;
  }

  public void setConsumerNonDurables(float consumerNonDurables) {
    this.consumerNonDurables = consumerNonDurables;
  }

  public float getIntermediateGoods() {
    return intermediateGoods;
  }

  public void setIntermediateGoods(float intermediateGoods) {
    this.intermediateGoods = intermediateGoods;
  }

  public float getElectricity() {
    return electricity;
  }

  public void setElectricity(float electricity) {
    this.electricity = electricity;
  }

  public float getConsumerDurables() {
    return consumerDurables;
  }

  public void setConsumerDurables(float consumerDurables) {
    this.consumerDurables = consumerDurables;
  }

  public float getPrimaryGoods() {
    return primaryGoods;
  }

  public void setPrimaryGoods(float primaryGoods) {
    this.primaryGoods = primaryGoods;
  }

  public float getBasicGoods() {
    return basicGoods;
  }

  public void setBasicGoods(float basicGoods) {
    this.basicGoods = basicGoods;
  }

  public float getGeneral() {
    return general;
  }

  public void setGeneral(float general) {
    this.general = general;
  }

  public float getCapitalGoods() {
    return capitalGoods;
  }

  public void setCapitalGoods(float capitalGoods) {
    this.capitalGoods = capitalGoods;
  }

  public float getManufactureOfTextiles() {
    return manufactureOfTextiles;
  }

  public void setManufactureOfTextiles(float manufactureOfTextiles) {
    this.manufactureOfTextiles = manufactureOfTextiles;
  }

  public float getManufactureOfCottonTextiles() {
    return manufactureOfCottonTextiles;
  }

  public void setManufactureOfCottonTextiles(float manufactureOfCottonTextiles) {
    this.manufactureOfCottonTextiles = manufactureOfCottonTextiles;
  }

  public float getManufactureOfWoolSilkAndManMadeFibreTextiles() {
    return manufactureOfWoolSilkAndManMadeFibreTextiles;
  }

  public void setManufactureOfWoolSilkAndManMadeFibreTextiles(float manufactureOfWoolSilkAndManMadeFibreTextiles) {
    this.manufactureOfWoolSilkAndManMadeFibreTextiles = manufactureOfWoolSilkAndManMadeFibreTextiles;
  }

  public float getManufactureOfTextileProductsIncludingWearingApparel() {
    return manufactureOfTextileProductsIncludingWearingApparel;
  }

  public void setManufactureOfTextileProductsIncludingWearingApparel(float manufactureOfTextileProductsIncludingWearingApparel) {
    this.manufactureOfTextileProductsIncludingWearingApparel = manufactureOfTextileProductsIncludingWearingApparel;
  }

  public float getManufactureOfWearingApparel() {
    return manufactureOfWearingApparel;
  }

  public void setManufactureOfWearingApparel(float manufactureOfWearingApparel) {
    this.manufactureOfWearingApparel = manufactureOfWearingApparel;
  }

  public float getManufactureOfLeatherAndRelatedProducts() {
    return manufactureOfLeatherAndRelatedProducts;
  }

  public void setManufactureOfLeatherAndRelatedProducts(float manufactureOfLeatherAndRelatedProducts) {
    this.manufactureOfLeatherAndRelatedProducts = manufactureOfLeatherAndRelatedProducts;
  }

  public float getManufactureOfWoodAndProductsOfWoodAndCorkExceptFurnitureManufactureOfArticlesOfStrawAndPlaitingMaterials() {
    return manufactureOfWoodAndProductsOfWoodAndCorkExceptFurnitureManufactureOfArticlesOfStrawAndPlaitingMaterials;
  }

  public void setManufactureOfWoodAndProductsOfWoodAndCorkExceptFurnitureManufactureOfArticlesOfStrawAndPlaitingMaterials(float manufactureOfWoodAndProductsOfWoodAndCorkExceptFurnitureManufactureOfArticlesOfStrawAndPlaitingMaterials) {
    this.manufactureOfWoodAndProductsOfWoodAndCorkExceptFurnitureManufactureOfArticlesOfStrawAndPlaitingMaterials = manufactureOfWoodAndProductsOfWoodAndCorkExceptFurnitureManufactureOfArticlesOfStrawAndPlaitingMaterials;
  }

  public float getManufactureOfPaperAndPaperProducts() {
    return manufactureOfPaperAndPaperProducts;
  }

  public void setManufactureOfPaperAndPaperProducts(float manufactureOfPaperAndPaperProducts) {
    this.manufactureOfPaperAndPaperProducts = manufactureOfPaperAndPaperProducts;
  }

  public float getPrintingAndReproductionOfRecordedMedia() {
    return printingAndReproductionOfRecordedMedia;
  }

  public void setPrintingAndReproductionOfRecordedMedia(float printingAndReproductionOfRecordedMedia) {
    this.printingAndReproductionOfRecordedMedia = printingAndReproductionOfRecordedMedia;
  }

  public float getManufactureOfCokeAndRefinedPetroleumProducts() {
    return manufactureOfCokeAndRefinedPetroleumProducts;
  }

  public void setManufactureOfCokeAndRefinedPetroleumProducts(float manufactureOfCokeAndRefinedPetroleumProducts) {
    this.manufactureOfCokeAndRefinedPetroleumProducts = manufactureOfCokeAndRefinedPetroleumProducts;
  }

  public float getManufactureOfChemicalsAndChemicalProducts() {
    return manufactureOfChemicalsAndChemicalProducts;
  }

  public void setManufactureOfChemicalsAndChemicalProducts(float manufactureOfChemicalsAndChemicalProducts) {
    this.manufactureOfChemicalsAndChemicalProducts = manufactureOfChemicalsAndChemicalProducts;
  }

  public float getManufactureOfPharmaceuticalsMedicinalChemicalAndBotanicalProducts() {
    return manufactureOfPharmaceuticalsMedicinalChemicalAndBotanicalProducts;
  }

  public void setManufactureOfPharmaceuticalsMedicinalChemicalAndBotanicalProducts(float manufactureOfPharmaceuticalsMedicinalChemicalAndBotanicalProducts) {
    this.manufactureOfPharmaceuticalsMedicinalChemicalAndBotanicalProducts = manufactureOfPharmaceuticalsMedicinalChemicalAndBotanicalProducts;
  }

  public float getManufactureOfRubberAndPlasticsProducts() {
    return manufactureOfRubberAndPlasticsProducts;
  }

  public void setManufactureOfRubberAndPlasticsProducts(float manufactureOfRubberAndPlasticsProducts) {
    this.manufactureOfRubberAndPlasticsProducts = manufactureOfRubberAndPlasticsProducts;
  }

  public float getManufactureOfOtherNonMetallicMineralProducts() {
    return manufactureOfOtherNonMetallicMineralProducts;
  }

  public void setManufactureOfOtherNonMetallicMineralProducts(float manufactureOfOtherNonMetallicMineralProducts) {
    this.manufactureOfOtherNonMetallicMineralProducts = manufactureOfOtherNonMetallicMineralProducts;
  }

  public float getManufactureOfBasicMetals() {
    return manufactureOfBasicMetals;
  }

  public void setManufactureOfBasicMetals(float manufactureOfBasicMetals) {
    this.manufactureOfBasicMetals = manufactureOfBasicMetals;
  }

  public float getManufactureOfFabricatedMetalProductsExceptMachineryAndEquipment() {
    return manufactureOfFabricatedMetalProductsExceptMachineryAndEquipment;
  }

  public void setManufactureOfFabricatedMetalProductsExceptMachineryAndEquipment(float manufactureOfFabricatedMetalProductsExceptMachineryAndEquipment) {
    this.manufactureOfFabricatedMetalProductsExceptMachineryAndEquipment = manufactureOfFabricatedMetalProductsExceptMachineryAndEquipment;
  }

  public float getManufactureOfComputerElectronicAndOpticalProducts() {
    return manufactureOfComputerElectronicAndOpticalProducts;
  }

  public void setManufactureOfComputerElectronicAndOpticalProducts(float manufactureOfComputerElectronicAndOpticalProducts) {
    this.manufactureOfComputerElectronicAndOpticalProducts = manufactureOfComputerElectronicAndOpticalProducts;
  }

  public float getManufactureOfElectricalEquipment() {
    return manufactureOfElectricalEquipment;
  }

  public void setManufactureOfElectricalEquipment(float manufactureOfElectricalEquipment) {
    this.manufactureOfElectricalEquipment = manufactureOfElectricalEquipment;
  }

  public float getManufactureOfMachineryAndEquipmentNEC() {
    return manufactureOfMachineryAndEquipmentNEC;
  }

  public void setManufactureOfMachineryAndEquipmentNEC(float manufactureOfMachineryAndEquipmentNEC) {
    this.manufactureOfMachineryAndEquipmentNEC = manufactureOfMachineryAndEquipmentNEC;
  }

  public float getManufactureOfMotorVehiclesTrailersAndSemiTrailers() {
    return manufactureOfMotorVehiclesTrailersAndSemiTrailers;
  }

  public void setManufactureOfMotorVehiclesTrailersAndSemiTrailers(float manufactureOfMotorVehiclesTrailersAndSemiTrailers) {
    this.manufactureOfMotorVehiclesTrailersAndSemiTrailers = manufactureOfMotorVehiclesTrailersAndSemiTrailers;
  }

  public float getManufactureOfOtherTransportEquipment() {
    return manufactureOfOtherTransportEquipment;
  }

  public void setManufactureOfOtherTransportEquipment(float manufactureOfOtherTransportEquipment) {
    this.manufactureOfOtherTransportEquipment = manufactureOfOtherTransportEquipment;
  }

  public float getManufactureOfFurniture() {
    return manufactureOfFurniture;
  }

  public void setManufactureOfFurniture(float manufactureOfFurniture) {
    this.manufactureOfFurniture = manufactureOfFurniture;
  }

  public float getOtherManufacturing() {
    return otherManufacturing;
  }

  public void setOtherManufacturing(float otherManufacturing) {
    this.otherManufacturing = otherManufacturing;
  }

  public float getManufactureOfTransportEquipmentAndParts() {
    return manufactureOfTransportEquipmentAndParts;
  }

  public void setManufactureOfTransportEquipmentAndParts(float manufactureOfTransportEquipmentAndParts) {
    this.manufactureOfTransportEquipmentAndParts = manufactureOfTransportEquipmentAndParts;
  }

  public float getManufactureOfRubberPlasticPetroleumAndCoalProductsProcessingOfNuclearFuels() {
    return manufactureOfRubberPlasticPetroleumAndCoalProductsProcessingOfNuclearFuels;
  }

  public void setManufactureOfRubberPlasticPetroleumAndCoalProductsProcessingOfNuclearFuels(float manufactureOfRubberPlasticPetroleumAndCoalProductsProcessingOfNuclearFuels) {
    this.manufactureOfRubberPlasticPetroleumAndCoalProductsProcessingOfNuclearFuels = manufactureOfRubberPlasticPetroleumAndCoalProductsProcessingOfNuclearFuels;
  }

  public float getManufactureOfWoodAndWoodProductsFurnitureAndFixtures() {
    return manufactureOfWoodAndWoodProductsFurnitureAndFixtures;
  }

  public void setManufactureOfWoodAndWoodProductsFurnitureAndFixtures(float manufactureOfWoodAndWoodProductsFurnitureAndFixtures) {
    this.manufactureOfWoodAndWoodProductsFurnitureAndFixtures = manufactureOfWoodAndWoodProductsFurnitureAndFixtures;
  }

  public float getManufactureOfBasicChemicalsAndChemicalProX0002DuctsExceptProductsOfPetroleumAndCoal() {
    return manufactureOfBasicChemicalsAndChemicalProX0002DuctsExceptProductsOfPetroleumAndCoal;
  }

  public void setManufactureOfBasicChemicalsAndChemicalProX0002DuctsExceptProductsOfPetroleumAndCoal(float manufactureOfBasicChemicalsAndChemicalProX0002DuctsExceptProductsOfPetroleumAndCoal) {
    this.manufactureOfBasicChemicalsAndChemicalProX0002DuctsExceptProductsOfPetroleumAndCoal = manufactureOfBasicChemicalsAndChemicalProX0002DuctsExceptProductsOfPetroleumAndCoal;
  }

  public float getManufactureOfNonMetallicMineralProducts() {
    return manufactureOfNonMetallicMineralProducts;
  }

  public void setManufactureOfNonMetallicMineralProducts(float manufactureOfNonMetallicMineralProducts) {
    this.manufactureOfNonMetallicMineralProducts = manufactureOfNonMetallicMineralProducts;
  }

  public float getBasicMetalAndAlloysIndustries() {
    return basicMetalAndAlloysIndustries;
  }

  public void setBasicMetalAndAlloysIndustries(float basicMetalAndAlloysIndustries) {
    this.basicMetalAndAlloysIndustries = basicMetalAndAlloysIndustries;
  }

  public float getManufactureOfMetalProductsAndPartsExceptMachineryAndEquipment() {
    return manufactureOfMetalProductsAndPartsExceptMachineryAndEquipment;
  }

  public void setManufactureOfMetalProductsAndPartsExceptMachineryAndEquipment(float manufactureOfMetalProductsAndPartsExceptMachineryAndEquipment) {
    this.manufactureOfMetalProductsAndPartsExceptMachineryAndEquipment = manufactureOfMetalProductsAndPartsExceptMachineryAndEquipment;
  }

  public float getManufacturingOfMachineryAndEquipmentOtherThanTransportManufactureequipmentOfMachinery() {
    return manufacturingOfMachineryAndEquipmentOtherThanTransportManufactureequipmentOfMachinery;
  }

  public void setManufacturingOfMachineryAndEquipmentOtherThanTransportManufactureequipmentOfMachinery(float manufacturingOfMachineryAndEquipmentOtherThanTransportManufactureequipmentOfMachinery) {
    this.manufacturingOfMachineryAndEquipmentOtherThanTransportManufactureequipmentOfMachinery = manufacturingOfMachineryAndEquipmentOtherThanTransportManufactureequipmentOfMachinery;
  }

  public float getManufactureOfJuteAndOtherVegetableFibreTexliiesExceptCotton() {
    return manufactureOfJuteAndOtherVegetableFibreTexliiesExceptCotton;
  }

  public void setManufactureOfJuteAndOtherVegetableFibreTexliiesExceptCotton(float manufactureOfJuteAndOtherVegetableFibreTexliiesExceptCotton) {
    this.manufactureOfJuteAndOtherVegetableFibreTexliiesExceptCotton = manufactureOfJuteAndOtherVegetableFibreTexliiesExceptCotton;
  }

  public float getManufactureOfPaperAndPaperProductsAndPrintingPublishingAndAlliedIndustries() {
    return manufactureOfPaperAndPaperProductsAndPrintingPublishingAndAlliedIndustries;
  }

  public void setManufactureOfPaperAndPaperProductsAndPrintingPublishingAndAlliedIndustries(float manufactureOfPaperAndPaperProductsAndPrintingPublishingAndAlliedIndustries) {
    this.manufactureOfPaperAndPaperProductsAndPrintingPublishingAndAlliedIndustries = manufactureOfPaperAndPaperProductsAndPrintingPublishingAndAlliedIndustries;
  }

  public float getManufactureOfLeatherAndProductsOfLeatherFurAndSubstitutesOfLeather() {
    return manufactureOfLeatherAndProductsOfLeatherFurAndSubstitutesOfLeather;
  }

  public void setManufactureOfLeatherAndProductsOfLeatherFurAndSubstitutesOfLeather(float manufactureOfLeatherAndProductsOfLeatherFurAndSubstitutesOfLeather) {
    this.manufactureOfLeatherAndProductsOfLeatherFurAndSubstitutesOfLeather = manufactureOfLeatherAndProductsOfLeatherFurAndSubstitutesOfLeather;
  }

  public float getManufactureOfMedicalPrecisionAndOpticalInstrumentsWatchesAndClocks() {
    return manufactureOfMedicalPrecisionAndOpticalInstrumentsWatchesAndClocks;
  }

  public void setManufactureOfMedicalPrecisionAndOpticalInstrumentsWatchesAndClocks(float manufactureOfMedicalPrecisionAndOpticalInstrumentsWatchesAndClocks) {
    this.manufactureOfMedicalPrecisionAndOpticalInstrumentsWatchesAndClocks = manufactureOfMedicalPrecisionAndOpticalInstrumentsWatchesAndClocks;
  }

  public float getManufactureOfRadioTelevisionAndCommunicationEquipmentAndApparatus() {
    return manufactureOfRadioTelevisionAndCommunicationEquipmentAndApparatus;
  }

  public void setManufactureOfRadioTelevisionAndCommunicationEquipmentAndApparatus(float manufactureOfRadioTelevisionAndCommunicationEquipmentAndApparatus) {
    this.manufactureOfRadioTelevisionAndCommunicationEquipmentAndApparatus = manufactureOfRadioTelevisionAndCommunicationEquipmentAndApparatus;
  }

  public float getTanningAndDressingOfLeatherManufactureOfLuggageHandbagsSaddleryHarnessAndFootwear() {
    return tanningAndDressingOfLeatherManufactureOfLuggageHandbagsSaddleryHarnessAndFootwear;
  }

  public void setTanningAndDressingOfLeatherManufactureOfLuggageHandbagsSaddleryHarnessAndFootwear(float tanningAndDressingOfLeatherManufactureOfLuggageHandbagsSaddleryHarnessAndFootwear) {
    this.tanningAndDressingOfLeatherManufactureOfLuggageHandbagsSaddleryHarnessAndFootwear = tanningAndDressingOfLeatherManufactureOfLuggageHandbagsSaddleryHarnessAndFootwear;
  }

  public float getManufactureOfBeveragesTobaccoAndRelatedProducts() {
    return manufactureOfBeveragesTobaccoAndRelatedProducts;
  }

  public void setManufactureOfBeveragesTobaccoAndRelatedProducts(float manufactureOfBeveragesTobaccoAndRelatedProducts) {
    this.manufactureOfBeveragesTobaccoAndRelatedProducts = manufactureOfBeveragesTobaccoAndRelatedProducts;
  }

  public float getManufactureOfFoodProducts() {
    return manufactureOfFoodProducts;
  }

  public void setManufactureOfFoodProducts(float manufactureOfFoodProducts) {
    this.manufactureOfFoodProducts = manufactureOfFoodProducts;
  }

  public float getManufactureOfBeverages() {
    return manufactureOfBeverages;
  }

  public void setManufactureOfBeverages(float manufactureOfBeverages) {
    this.manufactureOfBeverages = manufactureOfBeverages;
  }

  public float getManufactureOfTobaccoProducts() {
    return manufactureOfTobaccoProducts;
  }

  public void setManufactureOfTobaccoProducts(float manufactureOfTobaccoProducts) {
    this.manufactureOfTobaccoProducts = manufactureOfTobaccoProducts;
  }
  public float getInfrastructureConstructionGoods() {
    return infrastructureConstructionGoods;
  }

  public void setInfrastructureConstructionGoods(float infrastructureConstructionGoods) {
    this.infrastructureConstructionGoods = infrastructureConstructionGoods;
  }


  private UUID id;
  private int month;
  private int year;
  private int baseYear;

  private float mining;
  private float consumerNonDurables;
  private float intermediateGoods;
  private float electricity;
  private float consumerDurables;
  private float primaryGoods;
  private float basicGoods;
  private float infrastructureConstructionGoods;
  private float general;
  private float capitalGoods;

  private float manufactureOfTextiles;;
  private float manufactureOfCottonTextiles;
  private float manufactureOfWoolSilkAndManMadeFibreTextiles;
  private float manufactureOfTextileProductsIncludingWearingApparel;
  private float manufactureOfWearingApparel;
  private float manufactureOfLeatherAndRelatedProducts;
  private float manufactureOfWoodAndProductsOfWoodAndCorkExceptFurnitureManufactureOfArticlesOfStrawAndPlaitingMaterials;
  private float manufactureOfPaperAndPaperProducts;
  private float printingAndReproductionOfRecordedMedia;
  private float manufactureOfCokeAndRefinedPetroleumProducts;
  private float manufactureOfChemicalsAndChemicalProducts;
  private float manufactureOfPharmaceuticalsMedicinalChemicalAndBotanicalProducts;
  private float manufactureOfRubberAndPlasticsProducts;
  private float manufactureOfOtherNonMetallicMineralProducts;
  private float manufactureOfBasicMetals;
  private float manufactureOfFabricatedMetalProductsExceptMachineryAndEquipment;
  private float manufactureOfComputerElectronicAndOpticalProducts;
  private float manufactureOfElectricalEquipment;
  private float manufactureOfMachineryAndEquipmentNEC;
  private float manufactureOfMotorVehiclesTrailersAndSemiTrailers;
  private float manufactureOfOtherTransportEquipment;
  private float manufactureOfFurniture;
  private float otherManufacturing;
  private float manufactureOfTransportEquipmentAndParts;
  private float manufactureOfRubberPlasticPetroleumAndCoalProductsProcessingOfNuclearFuels;
  private float manufactureOfWoodAndWoodProductsFurnitureAndFixtures;
  private float manufactureOfBasicChemicalsAndChemicalProX0002DuctsExceptProductsOfPetroleumAndCoal;
  private float manufactureOfNonMetallicMineralProducts;
  private float basicMetalAndAlloysIndustries;
  private float manufactureOfMetalProductsAndPartsExceptMachineryAndEquipment;
  private float manufacturingOfMachineryAndEquipmentOtherThanTransportManufactureequipmentOfMachinery;
  private float manufactureOfJuteAndOtherVegetableFibreTexliiesExceptCotton;
  private float manufactureOfPaperAndPaperProductsAndPrintingPublishingAndAlliedIndustries;
  private float manufactureOfLeatherAndProductsOfLeatherFurAndSubstitutesOfLeather;
  private float manufactureOfMedicalPrecisionAndOpticalInstrumentsWatchesAndClocks;
  private float manufactureOfRadioTelevisionAndCommunicationEquipmentAndApparatus;
  private float tanningAndDressingOfLeatherManufactureOfLuggageHandbagsSaddleryHarnessAndFootwear;
  private float manufactureOfBeveragesTobaccoAndRelatedProducts;
  private float manufactureOfFoodProducts;
  private float manufactureOfBeverages;
  private float manufactureOfTobaccoProducts;

}
