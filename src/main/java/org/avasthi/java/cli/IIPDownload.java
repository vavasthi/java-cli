package org.avasthi.java.cli;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.avasthi.java.cli.pojos.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IIPDownload extends Base{

  PrintWriter pw =  new PrintWriter(new FileWriter(new File("cpi-groups-subgroups.txt")));
  Map<String, IipCategory> categoryMap = new HashMap<>();
  Map<String, IipSubCategory> subCategoryMap = new HashMap<>();

  public IIPDownload() throws IOException {
    for (IipCategory ic : IipCategory.values()) {
      categoryMap.put(ic.getCategory(), ic);
    }
    for (IipSubCategory isc : IipSubCategory.values()) {
      subCategoryMap.put(isc.getSubCategory(), isc);
    }
    addAdditionalTextMappings();
  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {



    IIPDownload iip = new IIPDownload();
    iip.download();
  }

  private void download() throws InterruptedException, ParseException {
    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<Iip> iipMongoCollection = db.getCollection(iipCollectionName, Iip.class);
    Map<String, Iip> map = new HashMap<>();
    for (IIPRanges iipRange : IIPRanges.values()) {

      for (int y = iipRange.getStartYear(); y <= iipRange.getEndYear(); ++y) {

        IIPResponse response = getPage(iipRange.getBaseYear(), y, 1);
        getData(response, iipRange.getStartYear(), map);
        if (response.getMeta_data() == null) {
          System.out.println(String.format("Metadata is null for year %d and page %d response = ", y, 1, response.toString()));
          break;
        }
        int totalPages = response.getMeta_data().totalPages();
        for (int page=2; page <= totalPages; ++page) {
          response = getPage(iipRange.getBaseYear(), y, page);
          getData(response, iipRange.getStartYear(), map);
          Thread.sleep(250);
        }
        iipMongoCollection.insertMany(map.values().stream().toList());
        map.clear();
      }
    }
  }
  private void getData(IIPResponse response, int startYear, Map<String, Iip> map) throws ParseException {
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
    Calendar calendar = Calendar.getInstance();
    if (response.getData() != null) {

      for (IIPResponse.SIngleInstance si : response.getData()) {
        if (si.index() != null) {

          IipCategory category = categoryMap.get(si.category());
          if (category == null) {
            System.out.println("Category Missing :" + si.category());
          }
          IipSubCategory subCategory = subCategoryMap.get(si.sub_category());
          if (subCategory == null) {
            System.out.println("Sub-category Missing :" + si.sub_category());
          }
          int year = si.year();
          calendar.setTime(monthFormat.parse(si.month()));
          int month = calendar.get(Calendar.MONTH);
          String key = String.format("%d-%d", month, year);
          Iip iip = map.get(key);
          if (iip == null) {
            iip = new Iip(month, year, startYear);
            map.put(key, iip);
          }
          setField(iip, category, subCategory, Float.parseFloat(si.index()));
        }
      }
    }
  }
  private void setField(final Iip iip, IipCategory category, IipSubCategory subCategory, float value) {
    switch(category) {
      case BASIC_GOODS -> iip.setBasicGoods(value);
      case MINING -> iip.setMining(value);
      case ELECTRICITY -> iip.setElectricity(value);
      case GENERAL -> iip.setGeneral(value);
      case PRIMARY_GOODS -> iip.setPrimaryGoods(value);
      case CAPITAL_GOODS -> iip.setCapitalGoods(value);
      case INTERMEDIATE_GOODS  -> iip.setIntermediateGoods(value);
      case INFRASTRUCTURE_CONSTRUCTION_GOODS -> iip.setInfrastructureConstructionGoods(value);
      case CONSUMER_DURABLES -> iip.setConsumerDurables(value);
      case CONSUMER_NON_DURABLES -> iip.setConsumerNonDurables(value);
      case MANUFACTURING -> {
        switch (subCategory) {
          case   MANUFACTURE_OF_TEXTILES -> iip.setManufactureOfTextiles(value);
          case   MANUFACTURE_OF_COTTON_TEXTILES -> iip.setManufactureOfCottonTextiles(value);
          case   MANUFACTURE_OF_WOOL_SILK_AND_MAN_MADE_FIBRE_TEXTILES -> iip.setManufactureOfWoolSilkAndManMadeFibreTextiles(value);
          case   MANUFACTURE_OF_TEXTILE_PRODUCTS_INCLUDING_WEARING_APPAREL -> iip.setManufactureOfTextileProductsIncludingWearingApparel(value);
          case   MANUFACTURE_OF_WEARING_APPAREL -> iip.setManufactureOfWearingApparel(value);
          case   MANUFACTURE_OF_LEATHER_AND_RELATED_PRODUCTS -> iip.setManufactureOfLeatherAndRelatedProducts(value);
          case   MANUFACTURE_OF_WOOD_AND_PRODUCTS_OF_WOOD_AND_CORK_EXCEPT_FURNITURE_MANUFACTURE_OF_ARTICLES_OF_STRAW_AND_PLAITING_MATERIALS -> iip.setManufactureOfWoodAndProductsOfWoodAndCorkExceptFurnitureManufactureOfArticlesOfStrawAndPlaitingMaterials(value);
          case   MANUFACTURE_OF_PAPER_AND_PAPER_PRODUCTS -> iip.setManufactureOfPaperAndPaperProducts(value);
          case   PRINTING_AND_REPRODUCTION_OF_RECORDED_MEDIA -> iip.setPrintingAndReproductionOfRecordedMedia(value);
          case   MANUFACTURE_OF_COKE_AND_REFINED_PETROLEUM_PRODUCTS -> iip.setManufactureOfCokeAndRefinedPetroleumProducts(value);
          case   MANUFACTURE_OF_CHEMICALS_AND_CHEMICAL_PRODUCTS -> iip.setManufactureOfChemicalsAndChemicalProducts(value);
          case   MANUFACTURE_OF_PHARMACEUTICALS__MEDICINAL_CHEMICAL_AND_BOTANICAL_PRODUCTS -> iip.setManufactureOfPharmaceuticalsMedicinalChemicalAndBotanicalProducts(value);
          case   MANUFACTURE_OF_RUBBER_AND_PLASTICS_PRODUCTS -> iip.setManufactureOfRubberAndPlasticsProducts(value);
          case   MANUFACTURE_OF_OTHER_NON_METALLIC_MINERAL_PRODUCTS -> iip.setManufactureOfOtherNonMetallicMineralProducts(value);
          case   MANUFACTURE_OF_BASIC_METALS -> iip.setManufactureOfBasicMetals(value);
          case   MANUFACTURE_OF_FABRICATED_METAL_PRODUCTS_EXCEPT_MACHINERY_AND_EQUIPMENT -> iip.setManufactureOfFabricatedMetalProductsExceptMachineryAndEquipment(value);
          case   MANUFACTURE_OF_COMPUTER_ELECTRONIC_AND_OPTICAL_PRODUCTS -> iip.setManufactureOfComputerElectronicAndOpticalProducts(value);
          case   MANUFACTURE_OF_ELECTRICAL_EQUIPMENT -> iip.setManufactureOfElectricalEquipment(value);
          case   MANUFACTURE_OF_MACHINERY_AND_EQUIPMENT_N_E_C -> iip.setManufactureOfMachineryAndEquipmentNEC(value);
          case   MANUFACTURE_OF_MOTOR_VEHICLES_TRAILERS_AND_SEMI_TRAILERS -> iip.setManufactureOfMotorVehiclesTrailersAndSemiTrailers(value);
          case   MANUFACTURE_OF_OTHER_TRANSPORT_EQUIPMENT -> iip.setManufactureOfOtherTransportEquipment(value);
          case   MANUFACTURE_OF_FURNITURE -> iip.setManufactureOfFurniture(value);
          case   OTHER_MANUFACTURING -> iip.setOtherManufacturing(value);
          case   MANUFACTURE_OF_TRANSPORT_EQUIPMENT_AND_PARTS -> iip.setManufactureOfTransportEquipmentAndParts(value);
          case   MANUFACTURE_OF_RUBBER_PLASTIC_PETROLEUM_AND_COAL_PRODUCTS_PROCESSING_OF_NUCLEAR_FUELS -> iip.setManufactureOfRubberPlasticPetroleumAndCoalProductsProcessingOfNuclearFuels(value);
          case   MANUFACTURE_OF_WOOD_AND_WOOD_PRODUCTS_FURNITURE_AND_FIXTURES -> iip.setManufactureOfWoodAndWoodProductsFurnitureAndFixtures(value);
          case   MANUFACTURE_OF_BASIC_CHEMICALS_AND_CHEMICAL_PRO_X0002_DUCTS_EXCEPT_PRODUCTS_OF_PETROLEUM_AND_COAL -> iip.setManufactureOfBasicChemicalsAndChemicalProX0002DuctsExceptProductsOfPetroleumAndCoal(value);
          case   MANUFACTURE_OF_NON_METALLIC_MINERAL_PRODUCTS -> iip.setManufactureOfNonMetallicMineralProducts(value);
          case   BASIC_METAL_AND_ALLOYS_INDUSTRIES -> iip.setBasicMetalAndAlloysIndustries(value);
          case   MANUFACTURE_OF_METAL_PRODUCTS_AND_PARTS_EXCEPT_MACHINERY_AND_EQUIPMENT -> iip.setManufactureOfMetalProductsAndPartsExceptMachineryAndEquipment(value);
          case   MANUFACTURING_OF_MACHINERY_AND_EQUIPMENT_OTHER_THAN_TRANSPORT_MANUFACTUREEQUIPMENT_OF_MACHINERY -> iip.setManufacturingOfMachineryAndEquipmentOtherThanTransportManufactureequipmentOfMachinery(value);
          case   MANUFACTURE_OF_JUTE_AND_OTHER_VEGETABLE_FIBRE_TEXLIIES_EXCEPT_COTTON -> iip.setManufactureOfJuteAndOtherVegetableFibreTexliiesExceptCotton(value);
          case   MANUFACTURE_OF_PAPER_AND_PAPER_PRODUCTS_AND_PRINTING_PUBLISHING_AND_ALLIED_INDUSTRIES -> iip.setManufactureOfPaperAndPaperProductsAndPrintingPublishingAndAlliedIndustries(value);
          case   MANUFACTURE_OF_LEATHER_AND_PRODUCTS_OF_LEATHER_FUR_AND_SUBSTITUTES_OF_LEATHER -> iip.setManufactureOfLeatherAndProductsOfLeatherFurAndSubstitutesOfLeather(value);
          case   MANUFACTURE_OF_MEDICAL_PRECISION_AND_OPTICAL_INSTRUMENTS_WATCHES_AND_CLOCKS -> iip.setManufactureOfMedicalPrecisionAndOpticalInstrumentsWatchesAndClocks(value);
          case   MANUFACTURE_OF_RADIO_TELEVISION_AND_COMMUNICATION_EQUIPMENT_AND_APPARATUS -> iip.setManufactureOfRadioTelevisionAndCommunicationEquipmentAndApparatus(value);
          case   TANNING_AND_DRESSING_OF_LEATHER_MANUFACTURE_OF_LUGGAGE_HANDBAGS_SADDLERY_HARNESS_AND_FOOTWEAR -> iip.setTanningAndDressingOfLeatherManufactureOfLuggageHandbagsSaddleryHarnessAndFootwear(value);
          case   MANUFACTURE_OF_BEVERAGES_TOBACCO_AND_RELATED_PRODUCTS -> iip.setManufactureOfBeveragesTobaccoAndRelatedProducts(value);
          case  MANUFACTURE_OF_FOOD_PRODUCTS -> iip.setManufactureOfFoodProducts(value);
          case   MANUFACTURE_OF_BEVERAGES -> iip.setManufactureOfBeverages(value);
          case   MANUFACTURE_OF_TOBACCO_PRODUCTS -> iip.setManufactureOfTobaccoProducts(value);
        }
      }
      default -> {}
    }
  }
  private void populateGroups(CPIResponse response, Map<String, Set<String>> map) {

    if (response.getData() != null) {

      for (CPIResponse.SIngleInstance si : response.getData()) {
        Set<String> subGroups = map.get(si.group());
        if (subGroups == null) {
          subGroups = new HashSet<>();
          map.put(si.group(), subGroups);
        }
        subGroups.add(si.subgroup());
        pw.printf("%s,%s\n", si.group(), si.subgroup());
      }
    }
    else {
      System.out.println(response.getMsg());
    }
  }
  private IIPResponse getPage(String baseYear, int year, int page) throws InterruptedException {

    boolean success = true;
    while (success) {

      OkHttpClient client = new OkHttpClient();
      String url = "https://api.mospi.gov.in/api/iip/getIIPMonthly?base_year=%s&year=%d&type=All&page=%d&Format=JSON";
      Request request = new Request.Builder()
              .url(String.format(url, baseYear, year, page))
              .header("Accept", "*/*")
              .get()
              .build();
      Map<String, List<String>> groupSubgroup = new HashMap<>();
      try (Response response = client.newCall(request).execute()) {

        Gson gson = new Gson();
        IIPResponse iipResponse = gson.fromJson(response.body().charStream(), IIPResponse.class);
        return iipResponse;
      } catch (IOException e) {
        System.out.println(String.format("Connection failure: Retrying after 5 seconds " + url, baseYear, year, page));
        Thread.sleep(5000);
      }
    }
    return null;
  }
  private void addAdditionalTextMappings() {
    categoryMap.put("Intermediate Goods", IipCategory.INTERMEDIATE_GOODS);
    categoryMap.put("Consumer Non-durables", IipCategory.CONSUMER_NON_DURABLES);
    categoryMap.put("Consumer Durables", IipCategory.CONSUMER_DURABLES);
    categoryMap.put("Capital Goods", IipCategory.CAPITAL_GOODS);
    categoryMap.put("Primary Goods", IipCategory.PRIMARY_GOODS);
    categoryMap.put("Infrastructure/ Construction Goods", IipCategory.INFRASTRUCTURE_CONSTRUCTION_GOODS);

    subCategoryMap.put("Manufacture of Food Products", IipSubCategory.MANUFACTURE_OF_FOOD_PRODUCTS);
    subCategoryMap.put("Other Manufacturing Industries", IipSubCategory.OTHER_MANUFACTURING);
    subCategoryMap.put("Manufacture of Food Product", IipSubCategory.MANUFACTURE_OF_FOOD_PRODUCTS);
    subCategoryMap.put("Manufacture of Beverages, Tobacco and Related_x0002_products", IipSubCategory.MANUFACTURE_OF_BEVERAGES_TOBACCO_AND_RELATED_PRODUCTS);
    subCategoryMap.put("Manufacture of Wood and Products of Wood and Cork, Except Furniture", IipSubCategory.MANUFACTURE_OF_WOOD_AND_PRODUCTS_OF_WOOD_AND_CORK_EXCEPT_FURNITURE_MANUFACTURE_OF_ARTICLES_OF_STRAW_AND_PLAITING_MATERIALS);
    subCategoryMap.put("Manufacture of Beverages", IipSubCategory.MANUFACTURE_OF_BEVERAGES);
    subCategoryMap.put("Manufacture of Tobacco Products", IipSubCategory.MANUFACTURE_OF_TOBACCO_PRODUCTS);
    subCategoryMap.put("Manufacture of Textiles", IipSubCategory.MANUFACTURE_OF_TEXTILES);
    subCategoryMap.put("Manufacture of Wearing Apparel", IipSubCategory.MANUFACTURE_OF_WEARING_APPAREL);
    subCategoryMap.put("Manufacture of Leather and Related Products", IipSubCategory.MANUFACTURE_OF_LEATHER_AND_RELATED_PRODUCTS);
    subCategoryMap.put("Manufacture of Paper and Paper Products", IipSubCategory.MANUFACTURE_OF_PAPER_AND_PAPER_PRODUCTS);
    subCategoryMap.put("Printing and Reproduction of Recorded Media", IipSubCategory.PRINTING_AND_REPRODUCTION_OF_RECORDED_MEDIA);
    subCategoryMap.put("Manufacture of Coke and Refined Petroleum Products", IipSubCategory.MANUFACTURE_OF_COKE_AND_REFINED_PETROLEUM_PRODUCTS);
    subCategoryMap.put("Manufacture of Chemicals and Chemical Products", IipSubCategory.MANUFACTURE_OF_CHEMICALS_AND_CHEMICAL_PRODUCTS);
    subCategoryMap.put("Manufacture of Pharmaceuticals, Medicinal Chemical and Botanical Products", IipSubCategory.MANUFACTURE_OF_PHARMACEUTICALS__MEDICINAL_CHEMICAL_AND_BOTANICAL_PRODUCTS);
    subCategoryMap.put("Manufacture of Rubber and Plastics Products", IipSubCategory.MANUFACTURE_OF_RUBBER_AND_PLASTICS_PRODUCTS);
    subCategoryMap.put("Manufacture of Other Non-metallic Mineral Products", IipSubCategory.MANUFACTURE_OF_NON_METALLIC_MINERAL_PRODUCTS);
    subCategoryMap.put("Manufacture of Basic Metals", IipSubCategory.MANUFACTURE_OF_BASIC_METALS);
    subCategoryMap.put("Manufacture of Fabricated Metal Products, Except Machinery and Equipment", IipSubCategory.MANUFACTURE_OF_FABRICATED_METAL_PRODUCTS_EXCEPT_MACHINERY_AND_EQUIPMENT);
    subCategoryMap.put("Manufacture of Computer, Electronic and Optical Products", IipSubCategory.MANUFACTURE_OF_COMPUTER_ELECTRONIC_AND_OPTICAL_PRODUCTS);
    subCategoryMap.put("Manufacture of Electrical Equipment", IipSubCategory.MANUFACTURE_OF_ELECTRICAL_EQUIPMENT);
    subCategoryMap.put("Manufacture of Machinery and Equipment N.e.c.", IipSubCategory.MANUFACTURE_OF_MACHINERY_AND_EQUIPMENT_N_E_C);
    subCategoryMap.put("Manufacture of Motor Vehicles, Trailers and Semi-trailers", IipSubCategory.MANUFACTURE_OF_MOTOR_VEHICLES_TRAILERS_AND_SEMI_TRAILERS);
    subCategoryMap.put("Manufacture of Other Transport Equipment", IipSubCategory.MANUFACTURE_OF_OTHER_TRANSPORT_EQUIPMENT);
    subCategoryMap.put("Manufacture of Furniture", IipSubCategory.MANUFACTURE_OF_FURNITURE);
    subCategoryMap.put("Other Manufacturing", IipSubCategory.OTHER_MANUFACTURING);
    subCategoryMap.put("Manufacture of Coke, Refined Petroleum Products and Nuclear Fuel",IipSubCategory.MANUFACTURE_OF_COKE_AND_REFINED_PETROLEUM_PRODUCTS);
    subCategoryMap.put("Manufacture of Electrical Machinery and Apparatus N.e.c.",IipSubCategory.MANUFACTURE_OF_ELECTRICAL_EQUIPMENT);
    subCategoryMap.put("Manufacture of Food Products and Beverages",IipSubCategory.MANUFACTURE_OF_FOOD_PRODUCTS);
    subCategoryMap.put("Manufacture of Furniture; Manufacturing N.e.c.",IipSubCategory.MANUFACTURE_OF_FURNITURE);
    subCategoryMap.put("Manufacture of Office, Accounting and Computing Machinery",IipSubCategory.MANUFACTURE_OF_COMPUTER_ELECTRONIC_AND_OPTICAL_PRODUCTS);
    subCategoryMap.put("Manufacture of Wearing Apparel; Dressing and Dyeing of Fur",IipSubCategory.MANUFACTURE_OF_WEARING_APPAREL);
    subCategoryMap.put("Manufacture of Wood and of Products of Wood and Cork, Except Furniture; Manufacture of Articles of Straw and Plaiting Materials",IipSubCategory.MANUFACTURE_OF_WOOD_AND_PRODUCTS_OF_WOOD_AND_CORK_EXCEPT_FURNITURE_MANUFACTURE_OF_ARTICLES_OF_STRAW_AND_PLAITING_MATERIALS);
    subCategoryMap.put("Publishing, Printing and Reproduction of Recorded Media",IipSubCategory.PRINTING_AND_REPRODUCTION_OF_RECORDED_MEDIA);
    subCategoryMap.put("Manufacture of Wood and Products of Wood and Cork, Except Furniture; Manufacture of Articles of Straw and Plaiting Materials", IipSubCategory.MANUFACTURE_OF_WOOD_AND_PRODUCTS_OF_WOOD_AND_CORK_EXCEPT_FURNITURE_MANUFACTURE_OF_ARTICLES_OF_STRAW_AND_PLAITING_MATERIALS);

  }
}
