package org.avasthi.java.cli.pojos;

import java.util.Date;

public enum QuarterlyFilingsHeads {
  PERIOD_ENDING("Period Ending", Date.class, DataType.DATE),
  PERIOD_BEGINNING("Period Beginning", Date.class, DataType.DATE),
  TYPE("Type", String.class, DataType.STRING),
  NO_OF_MONTHS("Number of Months", Integer.class, DataType.INTEGER),
  BASIC_EPS("Basic EPS after Extraordinary items"),
  CASH_EARNING_PER_SHARE("Cash Earnings Per Share"),
  DEPRECIATION ("Depreciation"),
  EPS("Earning Per Share"),
  EQUITY("EQUITY"),
  EXPENDITURE("Expenditure"),
  GROSS_PROFIT("Gross Profit"),
  INTEREST("Interest"),
  NET_PROFIT("Net Profit"),
  NET_PROFIT_MARGIN("Net Profit Margin"),
  NON_PROMOTER_SHARES("Non Promoter Shares", Float.class, DataType.NUMBER),
  PROMOTER_SHARES("Promoter Shares", Float.class, DataType.NUMBER),
  PROMOTER_PERCENTAGE_OF_SHARES("Promoter Percentage of Shares"),
  PROVISIONS_CONTINGENCIES("Provisions and Contingencies"),
  NS_IEOI("IEOI", String.class, DataType.STRING),
  OPERATING_PROFIT("Operating Profit"),
  OPERATING_PROFIT_MARGIN("Operating Profit Margin"),
  OTHER_INCOME("Other Income"),
  PAT("Profit After Tax"),
  PBT("Profit Before Tax"),
  RESERVES("Reserves"),
  RESULT_TYPE("Result Type", String.class, DataType.STRING),
  PROFIT_AND_LOSS_OF_ASSOCIATES("Profit And Loss of Associates"),
  TAX("Tax"),
  TOTAL_INCOME("Total Income");

  private final String head;
  private final DataType dataType;
  private final Class<?> clazz;

  QuarterlyFilingsHeads(String head, Class<?> clazz, DataType dataType) {
    this.head = head;
    this.clazz = clazz;
    this.dataType = dataType;
  }
  QuarterlyFilingsHeads(String head) {
    this.head = head;
    this.clazz = Float.class;
    this.dataType = DataType.NUMBER;
  }

  public String getHead() {
    return head;
  }

  public DataType getDataType() {
    return dataType;
  }

  public Class<?> getClazz() {
    return clazz;
  }
}
