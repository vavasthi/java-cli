package org.avasthi.java.cli.pojos;

public enum IipSubCategory {

  NONE(""),
  MANUFACTURE_OF_FOOD_PRODUCTS("Manufacture of food products"),
  MANUFACTURE_OF_BEVERAGES("Manufacture of beverages"),
  MANUFACTURE_OF_BEVERAGES_TOBACCO_AND_RELATED_PRODUCTS("MANUFACTURE OF BEVERAGES, TOBACCO AND RELATED_X0002_PRODUCTS"),
  MANUFACTURE_OF_TOBACCO_PRODUCTS("Manufacture of tobacco products"),
  MANUFACTURE_OF_TEXTILES("Manufacture of textiles"),
  MANUFACTURE_OF_COTTON_TEXTILES("Manufacture of Cotton Textiles"),
  MANUFACTURE_OF_WOOL_SILK_AND_MAN_MADE_FIBRE_TEXTILES("Manufacture of Wool Silk and Man-made Fibre Textiles_x0002_"),
  MANUFACTURE_OF_TEXTILE_PRODUCTS_INCLUDING_WEARING_APPAREL("Manufacture of Textile Products (including Wearing Apparel)"),
  MANUFACTURE_OF_WEARING_APPAREL("Manufacture of wearing apparel"),
  MANUFACTURE_OF_LEATHER_AND_RELATED_PRODUCTS("Manufacture of leather and related products"),
  MANUFACTURE_OF_WOOD_AND_PRODUCTS_OF_WOOD_AND_CORK_EXCEPT_FURNITURE_MANUFACTURE_OF_ARTICLES_OF_STRAW_AND_PLAITING_MATERIALS("Manufacture of wood and products of wood and cork, except furniture; manufacture of articles of straw and plaiting materials"),
  MANUFACTURE_OF_PAPER_AND_PAPER_PRODUCTS("Manufacture of paper and paper products"),
  PRINTING_AND_REPRODUCTION_OF_RECORDED_MEDIA("Printing and reproduction of recorded media"),
  MANUFACTURE_OF_COKE_AND_REFINED_PETROLEUM_PRODUCTS("Manufacture of coke and refined petroleum products"),
  MANUFACTURE_OF_CHEMICALS_AND_CHEMICAL_PRODUCTS("Manufacture of chemicals and chemical products"),
  MANUFACTURE_OF_PHARMACEUTICALS__MEDICINAL_CHEMICAL_AND_BOTANICAL_PRODUCTS("Manufacture of pharmaceuticals, medicinal chemical and botanical products"),
  MANUFACTURE_OF_RUBBER_AND_PLASTICS_PRODUCTS("Manufacture of rubber and plastics products"),
  MANUFACTURE_OF_OTHER_NON_METALLIC_MINERAL_PRODUCTS("Manufacture of other non-metallic mineral products"),
  MANUFACTURE_OF_BASIC_METALS("Manufacture of basic metals"),
  MANUFACTURE_OF_FABRICATED_METAL_PRODUCTS_EXCEPT_MACHINERY_AND_EQUIPMENT("Manufacture of fabricated metal products, except machinery and equipment"),
  MANUFACTURE_OF_COMPUTER_ELECTRONIC_AND_OPTICAL_PRODUCTS("Manufacture of computer, electronic and optical products"),
  MANUFACTURE_OF_ELECTRICAL_EQUIPMENT("Manufacture of electrical equipment"),
  MANUFACTURE_OF_MACHINERY_AND_EQUIPMENT_N_E_C("Manufacture of machinery and equipment n.e.c."),
  MANUFACTURE_OF_MOTOR_VEHICLES_TRAILERS_AND_SEMI_TRAILERS("Manufacture of motor vehicles, trailers and semi-trailers"),
  MANUFACTURE_OF_OTHER_TRANSPORT_EQUIPMENT("Manufacture of other transport equipment"),
  MANUFACTURE_OF_FURNITURE("Manufacture of furniture"),
  OTHER_MANUFACTURING("Other manufacturing"),
  MANUFACTURE_OF_TRANSPORT_EQUIPMENT_AND_PARTS("Manufacture of Transport Equipment and Parts"),
  MANUFACTURE_OF_RUBBER_PLASTIC_PETROLEUM_AND_COAL_PRODUCTS_PROCESSING_OF_NUCLEAR_FUELS("Manufacture of Rubber, Plastic, Petroleum and Coal Products; Processing of Nuclear Fuels"),
  MANUFACTURE_OF_WOOD_AND_WOOD_PRODUCTS_FURNITURE_AND_FIXTURES("Manufacture of Wood and Wood Products; Furniture and Fixtures"),
  MANUFACTURE_OF_BASIC_CHEMICALS_AND_CHEMICAL_PRO_X0002_DUCTS_EXCEPT_PRODUCTS_OF_PETROLEUM_AND_COAL("Manufacture of Basic Chemicals and Chemical Pro_x0002_ducts (except Products of Petroleum and Coal)"),
  MANUFACTURE_OF_NON_METALLIC_MINERAL_PRODUCTS("Manufacture of Non-metallic Mineral Products"),
  BASIC_METAL_AND_ALLOYS_INDUSTRIES("Basic Metal and Alloys Industries"),
  MANUFACTURE_OF_METAL_PRODUCTS_AND_PARTS_EXCEPT_MACHINERY_AND_EQUIPMENT("Manufacture of Metal Products and Parts, Except Machinery and Equipment"),
  MANUFACTURING_OF_MACHINERY_AND_EQUIPMENT_OTHER_THAN_TRANSPORT_MANUFACTUREEQUIPMENT_OF_MACHINERY("Manufacturing of Machinery and Equipment Other Than Transport Manufactureequipment of Machinery (manufacture and Equipment of Scientific Otherequip- Than Ment, Photographic! Cinematographic Equipment and Watches & Clocks is C1assifiedindivision 38)"),
  MANUFACTURE_OF_JUTE_AND_OTHER_VEGETABLE_FIBRE_TEXLIIES_EXCEPT_COTTON("Manufacture of Jute and Other Vegetable Fibre Texliies (except Cotton)"),
  MANUFACTURE_OF_PAPER_AND_PAPER_PRODUCTS_AND_PRINTING_PUBLISHING_AND_ALLIED_INDUSTRIES("Manufacture of Paper and Paper Products and Printing Publishing & Allied Industries"),
  MANUFACTURE_OF_LEATHER_AND_PRODUCTS_OF_LEATHER_FUR_AND_SUBSTITUTES_OF_LEATHER("Manufacture of Leather and Products of Leather, Fur &:: Substitutes of Leather"),
  MANUFACTURE_OF_MEDICAL_PRECISION_AND_OPTICAL_INSTRUMENTS_WATCHES_AND_CLOCKS("Manufacture of Medical, Precision and Optical Instruments, Watches and Clocks"),
  MANUFACTURE_OF_RADIO_TELEVISION_AND_COMMUNICATION_EQUIPMENT_AND_APPARATUS("Manufacture of Radio, Television and Communication Equipment and Apparatus"),
  TANNING_AND_DRESSING_OF_LEATHER_MANUFACTURE_OF_LUGGAGE_HANDBAGS_SADDLERY_HARNESS_AND_FOOTWEAR("Tanning and Dressing of Leather; Manufacture of Luggage, Handbags, Saddlery, Harness and Footwear"),

          ;

  private final String subCategory;

  IipSubCategory(String subCategory) {
    this.subCategory = subCategory;
  }

  public String getSubCategory() {
    return subCategory;
  }
}
