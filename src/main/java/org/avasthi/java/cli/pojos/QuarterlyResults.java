package org.avasthi.java.cli.pojos;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;

public class QuarterlyResults implements Serializable {
  public QuarterlyResults(UUID id,
                          String isin,
                          UUID companyId,
                          String symbol,
                          String stockCode,
                          Date periodEnding,
                          Date periodStarting,
                          String type,
                          int noOfMonths,
                          float basicEps,
                          float cashEarningPerShare,
                          float depreciation,
                          float eps,
                          float equity,
                          float expenditure,
                          float grossProfit,
                          float interest,
                          float netProfit,
                          float netProfitMargin,
                          float nonPromoterShares,
                          float promoterShares,
                          float promoterPercentageOfShares,
                          String IEOI,
                          float operatingProfit,
                          float operatingProfitMargin,
                          float otherIncome,
                          float profitAfterTax,
                          float profitBeforeTax,
                          float reserves,
                          String resultType,
                          float profitAndLossOfAssociates,
                          float tax,
                          float totalIncome) {
    this.id = id;
    this.isin = isin;
    this.companyId = companyId;
    this.symbol = symbol;
    this.stockCode = stockCode;
    this.periodEnding = periodEnding;
    this.periodStarting = periodStarting;
    this.type = type;
    this.noOfMonths = noOfMonths;
    this.basicEps = basicEps;
    this.cashEarningPerShare = cashEarningPerShare;
    this.depreciation = depreciation;
    this.eps = eps;
    this.equity = equity;
    this.expenditure = expenditure;
    this.grossProfit = grossProfit;
    this.interest = interest;
    this.netProfit = netProfit;
    this.netProfitMargin = netProfitMargin;
    this.nonPromoterShares = nonPromoterShares;
    this.promoterShares = promoterShares;
    this.promoterPercentageOfShares = promoterPercentageOfShares;
    this.IEOI = IEOI;
    this.operatingProfit = operatingProfit;
    this.operatingProfitMargin = operatingProfitMargin;
    this.otherIncome = otherIncome;
    this.profitAfterTax = profitAfterTax;
    this.profitBeforeTax = profitBeforeTax;
    this.reserves = reserves;
    this.resultType = resultType;
    this.profitAndLossOfAssociates = profitAndLossOfAssociates;
    this.tax = tax;
    this.totalIncome = totalIncome;
    Calendar starting = Calendar.getInstance();
    starting.setTime(periodStarting);
    Calendar ending = Calendar.getInstance();
    ending.setTime(periodEnding);
    if (starting.after(ending)) {
      starting.setTime(ending.getTime());
      starting.add(Calendar.MONTH, -3);
      starting.add(Calendar.DAY_OF_MONTH,1 );
      while(starting.get(Calendar.DAY_OF_MONTH) != 1) {

        starting.add(Calendar.DAY_OF_MONTH, 1);
      }
      this.periodStarting = starting.getTime();
    }

  }

  public QuarterlyResults(String isin, UUID companyId, String symbol, String stockCode, Map<QuarterlyFilingsHeads, Object> values) {
    this(UUID.randomUUID(),
            isin,
            companyId,
            symbol,
            stockCode,
            getDateValue(values, QuarterlyFilingsHeads.PERIOD_ENDING),
            getDateValue(values, QuarterlyFilingsHeads.PERIOD_BEGINNING),
            getStringValue(values, QuarterlyFilingsHeads.TYPE),
            getIntValue(values, QuarterlyFilingsHeads.NO_OF_MONTHS),
            getFloatValue(values, QuarterlyFilingsHeads.BASIC_EPS),
            getFloatValue(values, QuarterlyFilingsHeads.CASH_EARNING_PER_SHARE),
            getFloatValue(values, QuarterlyFilingsHeads.DEPRECIATION),
            getFloatValue(values, QuarterlyFilingsHeads.EPS),
            getFloatValue(values, QuarterlyFilingsHeads.EQUITY),
            getFloatValue(values, QuarterlyFilingsHeads.EXPENDITURE),
            getFloatValue(values, QuarterlyFilingsHeads.GROSS_PROFIT),
            getFloatValue(values, QuarterlyFilingsHeads.INTEREST),
            getFloatValue(values, QuarterlyFilingsHeads.NET_PROFIT),
            getFloatValue(values, QuarterlyFilingsHeads.NET_PROFIT_MARGIN),
            getFloatValue(values, QuarterlyFilingsHeads.NON_PROMOTER_SHARES),
            getFloatValue(values, QuarterlyFilingsHeads.PROMOTER_SHARES),
            getFloatValue(values, QuarterlyFilingsHeads.PROMOTER_PERCENTAGE_OF_SHARES),
            getStringValue(values, QuarterlyFilingsHeads.NS_IEOI),
            getFloatValue(values, QuarterlyFilingsHeads.OPERATING_PROFIT),
            getFloatValue(values, QuarterlyFilingsHeads.OPERATING_PROFIT_MARGIN),
            getFloatValue(values, QuarterlyFilingsHeads.OTHER_INCOME),
            getFloatValue(values, QuarterlyFilingsHeads.PAT),
            getFloatValue(values, QuarterlyFilingsHeads.PBT),
            getFloatValue(values, QuarterlyFilingsHeads.RESERVES),
            getStringValue(values, QuarterlyFilingsHeads.RESULT_TYPE),
            getFloatValue(values, QuarterlyFilingsHeads.PROFIT_AND_LOSS_OF_ASSOCIATES),
            getFloatValue(values, QuarterlyFilingsHeads.TAX),
            getFloatValue(values, QuarterlyFilingsHeads.TOTAL_INCOME));
  }
  public QuarterlyResults() {
  }

  private static int getIntValue(Map<QuarterlyFilingsHeads, Object> values, QuarterlyFilingsHeads head) {

    Object o = values.get(head);
    return o == null || o.equals("") ? 0 : Integer.class.cast(o);
  }
  private static float getFloatValue(Map<QuarterlyFilingsHeads, Object> values, QuarterlyFilingsHeads head) {

    Object o = values.get(head);
    return o == null || o.equals("") ? 0.0F : Float.class.cast(o);
  }
  private static Date getDateValue(Map<QuarterlyFilingsHeads, Object> values, QuarterlyFilingsHeads head) {

    Object o = values.get(head);
    return o == null || o.equals("") ? new Date() : Date.class.cast(o);
  }
  private static String getStringValue(Map<QuarterlyFilingsHeads, Object> values, QuarterlyFilingsHeads head) {

    Object o = values.get(head);
    return o == null || o.equals("") ? "" : String.class.cast(o);
  }
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public String getStockCode() {
    return stockCode;
  }

  public void setStockCode(String stockCode) {
    this.stockCode = stockCode;
  }

  public String getIsin() {
    return isin;
  }

  public void setIsin(String isin) {
    this.isin = isin;
  }

  public UUID getCompanyId() {
    return companyId;
  }

  public void setCompanyId(UUID companyId) {
    this.companyId = companyId;
  }

  public Date getPeriodEnding() {
    return periodEnding;
  }

  public void setPeriodEnding(Date periodEnding) {
    this.periodEnding = periodEnding;
  }

  public Date getPeriodStarting() {
    return periodStarting;
  }

  public void setPeriodStarting(Date periodStarting) {
    this.periodStarting = periodStarting;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getNoOfMonths() {
    return noOfMonths;
  }

  public void setNoOfMonths(int noOfMonths) {
    this.noOfMonths = noOfMonths;
  }

  public float getBasicEps() {
    return basicEps;
  }

  public void setBasicEps(float basicEps) {
    this.basicEps = basicEps;
  }

  public float getCashEarningPerShare() {
    return cashEarningPerShare;
  }

  public void setCashEarningPerShare(float cashEarningPerShare) {
    this.cashEarningPerShare = cashEarningPerShare;
  }

  public float getDepreciation() {
    return depreciation;
  }

  public void setDepreciation(float depreciation) {
    this.depreciation = depreciation;
  }

  public float getEps() {
    return eps;
  }

  public void setEps(float eps) {
    this.eps = eps;
  }

  public float getEquity() {
    return equity;
  }

  public void setEquity(float equity) {
    this.equity = equity;
  }

  public float getExpenditure() {
    return expenditure;
  }

  public void setExpenditure(float expenditure) {
    this.expenditure = expenditure;
  }

  public float getGrossProfit() {
    return grossProfit;
  }

  public void setGrossProfit(float grossProfit) {
    this.grossProfit = grossProfit;
  }

  public float getInterest() {
    return interest;
  }

  public void setInterest(float interest) {
    this.interest = interest;
  }

  public float getNetProfit() {
    return netProfit;
  }

  public void setNetProfit(float netProfit) {
    this.netProfit = netProfit;
  }

  public float getNetProfitMargin() {
    return netProfitMargin;
  }

  public void setNetProfitMargin(float netProfitMargin) {
    this.netProfitMargin = netProfitMargin;
  }

  public float getNonPromoterShares() {
    return nonPromoterShares;
  }

  public void setNonPromoterShares(float nonPromoterShares) {
    this.nonPromoterShares = nonPromoterShares;
  }

  public float getPromoterShares() {
    return promoterShares;
  }

  public void setPromoterShares(float promoterShares) {
    this.promoterShares = promoterShares;
  }

  public float getPromoterPercentageOfShares() {
    return promoterPercentageOfShares;
  }

  public void setPromoterPercentageOfShares(float promoterPercentageOfShares) {
    this.promoterPercentageOfShares = promoterPercentageOfShares;
  }

  public String getIEOI() {
    return IEOI;
  }

  public void setIEOI(String IEOI) {
    this.IEOI = IEOI;
  }

  public float getOperatingProfit() {
    return operatingProfit;
  }

  public void setOperatingProfit(float operatingProfit) {
    this.operatingProfit = operatingProfit;
  }

  public float getOperatingProfitMargin() {
    return operatingProfitMargin;
  }

  public void setOperatingProfitMargin(float operatingProfitMargin) {
    this.operatingProfitMargin = operatingProfitMargin;
  }

  public float getOtherIncome() {
    return otherIncome;
  }

  public void setOtherIncome(float otherIncome) {
    this.otherIncome = otherIncome;
  }

  public float getProfitAfterTax() {
    return profitAfterTax;
  }

  public void setProfitAfterTax(float profitAfterTax) {
    this.profitAfterTax = profitAfterTax;
  }

  public float getProfitBeforeTax() {
    return profitBeforeTax;
  }

  public void setProfitBeforeTax(float profitBeforeTax) {
    this.profitBeforeTax = profitBeforeTax;
  }

  public float getReserves() {
    return reserves;
  }

  public void setReserves(float reserves) {
    this.reserves = reserves;
  }

  public String getResultType() {
    return resultType;
  }

  public void setResultType(String resultType) {
    this.resultType = resultType;
  }

  public float getProfitAndLossOfAssociates() {
    return profitAndLossOfAssociates;
  }

  public void setProfitAndLossOfAssociates(float profitAndLossOfAssociates) {
    this.profitAndLossOfAssociates = profitAndLossOfAssociates;
  }

  public float getTax() {
    return tax;
  }

  public void setTax(float tax) {
    this.tax = tax;
  }

  public float getTotalIncome() {
    return totalIncome;
  }

  public void setTotalIncome(float totalIncome) {
    this.totalIncome = totalIncome;
  }

  @Override
  public String toString() {
    return "QuarterlyResults{" +
            "id=" + id +
            ", isin='" + isin + '\'' +
            ", companyId=" + companyId +
            ", symbol='" + symbol + '\'' +
            ", stockCode='" + stockCode + '\'' +
            ", periodEnding=" + periodEnding +
            ", periodStarting=" + periodStarting +
            ", type='" + type + '\'' +
            ", noOfMonths=" + noOfMonths +
            ", basicEps=" + basicEps +
            ", cashEarningPerShare=" + cashEarningPerShare +
            ", depreciation=" + depreciation +
            ", eps=" + eps +
            ", equity=" + equity +
            ", expenditure=" + expenditure +
            ", grossProfit=" + grossProfit +
            ", interest=" + interest +
            ", netProfit=" + netProfit +
            ", netProfitMargin=" + netProfitMargin +
            ", nonPromoterShares=" + nonPromoterShares +
            ", promoterShares=" + promoterShares +
            ", promoterPercentageOfShares=" + promoterPercentageOfShares +
            ", IEOI='" + IEOI + '\'' +
            ", operatingProfit=" + operatingProfit +
            ", operatingProfitMargin=" + operatingProfitMargin +
            ", otherIncome=" + otherIncome +
            ", profitAfterTax=" + profitAfterTax +
            ", profitBeforeTax=" + profitBeforeTax +
            ", reserves=" + reserves +
            ", resultType='" + resultType + '\'' +
            ", profitAndLossOfAssociates=" + profitAndLossOfAssociates +
            ", tax=" + tax +
            ", totalIncome=" + totalIncome +
            '}';
  }

  private UUID id;
  private String isin;
  private UUID companyId;
  private String symbol;
  private String stockCode;
  private Date periodEnding;
  private Date periodStarting;
  private String type;
  private int noOfMonths;
  private float basicEps;
  private float cashEarningPerShare;
  private float depreciation;
  private float eps;
  private float equity;
  private float expenditure;
  private float grossProfit;
  private float interest;
  private float netProfit;
  private float netProfitMargin;
  private float nonPromoterShares;
  private float promoterShares;
  private float promoterPercentageOfShares;
  private String IEOI;
  private float operatingProfit;
  private float operatingProfitMargin;
  private float otherIncome;
  private float profitAfterTax;
  private float profitBeforeTax;
  private float reserves;
  private String resultType;
  private float profitAndLossOfAssociates;
  private float tax;
  private float totalIncome;
}
