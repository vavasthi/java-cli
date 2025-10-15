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

public class CPIDownload extends Base{

  PrintWriter pw =  new PrintWriter(new FileWriter(new File("cpi-groups-subgroups.txt")));
  Map<String, CpiGroup> groupMap = new HashMap<>();
  Map<String, CpiSubGroup> subGroupMap = new HashMap<>();

  public CPIDownload() throws IOException {
    for (CpiGroup cg : CpiGroup.values()) {
      groupMap.put(cg.getGroup(), cg);
    }
    for (CpiSubGroup cg : CpiSubGroup.values()) {
      subGroupMap.put(cg.getSubGroup(), cg);
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {



    CPIDownload cpi = new CPIDownload();
    cpi.download();
  }

  private void download() throws InterruptedException, ParseException {
    MongoDatabase db = getMongoClient().getDatabase(database);
    MongoCollection<Cpi> cpiMongoCollection = db.getCollection(cpiCollectionName, Cpi.class);
    Map<String, Cpi> map = new HashMap<>();
    for (int year = 2011; year < 2013; ++year) {

      CPIResponse response = getPage(2012, year, "Back", 1);
      getData(response, map);
      if (response.getMeta_data() == null) {
        System.out.println(String.format("Metadata is null for year %d and page %d response = ", year, 1, response.toString()));
        break;
      }
      int totalPages = response.getMeta_data().totalPages();
      Map<String, Set<String>> groupSubgroup = new HashMap<>();
      populateGroups(response, groupSubgroup);
      for (int page=2; page <= totalPages; ++page) {
        response = getPage(2012, year, "Back", page);
        getData(response, map);
        Thread.sleep(250);
      }
      System.out.println(String.format("Writing %d records for year %d", map.size(), year));
      cpiMongoCollection.insertMany(map.values().stream().toList());
      map.clear();
    }
    for (int year = 2013; year < 2026; ++year) {

      CPIResponse response = getPage(2012, year, "Current", 1);
      getData(response, map);
      if (response.getMeta_data() == null) {
        System.out.println(String.format("Metadata is null for year %d and page %d response = ", year, 1, response.toString()));
        break;
      }
      int totalPages = response.getMeta_data().totalPages();
      Map<String, Set<String>> groupSubgroup = new HashMap<>();
      populateGroups(response, groupSubgroup);
      for (int page=2; page <= totalPages; ++page) {
        response = getPage(2012, year, "Current", page);
        getData(response, map);
        Thread.sleep(500);
      }
      System.out.println(String.format("Writing %d records for year %d", map.size(), year));
      cpiMongoCollection.insertMany(map.values().stream().toList());
      map.clear();
    }
  }
  private void getData(CPIResponse response, Map<String, Cpi> map) throws ParseException {
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
    Calendar calendar = Calendar.getInstance();
    if (response.getData() != null) {

      for (CPIResponse.SIngleInstance si : response.getData()) {
        if (si.index() != null) {

          CpiGroup group = groupMap.get(si.group());
          CpiSubGroup subGroup = subGroupMap.get(si.subgroup());
          int year = si.year();
          calendar.setTime(monthFormat.parse(si.month()));
          int month = calendar.get(Calendar.MONTH);
          String key = String.format("%d-%s", year, si.month());
          Cpi cpi = map.get(key);
          if (cpi == null) {
            cpi = new Cpi(month, year, si.state());
            map.put(key, cpi);
          }
          setField(cpi, group, subGroup, Float.parseFloat(si.index()));
        }
      }
    }
  }
  private void setField(final Cpi cpi, CpiGroup group, CpiSubGroup subGroup, float value) {
    switch (subGroup) {
      case CEREALS_AND_PRODUCTS -> cpi.setCerealsAndProducts(value);
      case CLOTHING -> cpi.setClothing(value);
      case CLOTHING_AND_FOOTWEAR_OVERALL -> cpi.setClothingAndFootwearOverall(value);
      case CONSUMER_FOOD_PRICE_OVERALL -> cpi.setConsumerFoodPriceOverall(value);
      case EDUCATION -> cpi.setEducation(value);
      case EGG -> cpi.setEgg(value);
      case FOOD_AND_BEVERAGES_OVERALL -> cpi.setFoodAndBeveragesOverall(value);
      case FOOTWEAR -> cpi.setFootwear(value);
      case FRUITS -> cpi.setFruits(value);
      case FUEL_AND_LIGHT_OVERALL -> cpi.setFuelAndLightOverall(value);
      case GENERAL_OVERALL -> cpi.setGeneralOverall(value);
      case HEALTH -> cpi.setHealth(value);
      case HOUSEHOLD_GOODS_AND_SERVICES -> cpi.setHouseholdGoodsAndServices(value);
      case HOUSING_OVERALL -> cpi.setHousingOverall(value);
      case MEAT_AND_FISH -> cpi.setMeatAndFish(value);
      case MILK_AND_PRODUCTS -> cpi.setMilkAndProducts(value);
      case MISCELLANEOUS_OVERALL -> cpi.setMiscellaneousOverall(value);
      case NON_ALCOHOLIC_BEVERAGES -> cpi.setNonAlcoholicBeverages(value);
      case OILS_AND_FATS -> cpi.setOilsAndFats(value);
      case PERSONAL_CARE_AND_EFFECTS -> cpi.setPersonalCareAndEffects(value);
      case PREPARED_MEALS -> cpi.setPreparedMeals(value);
      case PULSES_AND_PRODUCTS -> cpi.setPulsesAndProducts(value);
      case RECREATION_AND_AMUSEMENT -> cpi.setRecreationAndAmusement(value);
      case SPICES -> cpi.setSpices(value);
      case SUGAR_AND_CONFECTIONERY -> cpi.setSugerAndConfectionary(value);
      case PAN_TOBACCO_AND_INTOXICANTS_OVERALL -> cpi.setTobaccoAndIntoxicants(value);
      case TRANSPORT_AND_COMMUNICATION -> cpi.setTransportAndCommunicaation(value);
      case VEGETABLES -> cpi.setVegetables(value);
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
  private CPIResponse getPage(int baseYear, int year, String type, int page) throws InterruptedException {

    boolean success = true;
    while (success) {

      OkHttpClient client = new OkHttpClient();
      String url = "https://api.mospi.gov.in/api/cpi/getCPIIndex?base_year=%d&year=%d&state_code=99&series=%s&Format=JSON&page=%d";
      Request request = new Request.Builder()
              .url(String.format(url, baseYear, year, type, page))
              .header("Accept", "*/*")
              .get()
              .build();
      Map<String, List<String>> groupSubgroup = new HashMap<>();
      try (Response response = client.newCall(request).execute()) {

        Gson gson = new Gson();
        CPIResponse cpiResponse = gson.fromJson(response.body().charStream(), CPIResponse.class);
        return cpiResponse;
      } catch (IOException e) {
        System.out.println(String.format("Connection failure: Retrying after 5 seconds " + url, baseYear, year, type, page));
        Thread.sleep(5000);
      }
    }
    return null;
  }
}
