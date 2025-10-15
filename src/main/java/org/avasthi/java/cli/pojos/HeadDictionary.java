package org.avasthi.java.cli.pojos;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

public class HeadDictionary {

  public static final HeadDictionary INSTANCE = new HeadDictionary();
  private Map<String, QuarterlyFilingsHeads> headDictionary = Map.ofEntries(
          entry("Basic EPS after Extraordinary items",QuarterlyFilingsHeads.BASIC_EPS),
          entry("Cash Earnings Per Share",QuarterlyFilingsHeads.CASH_EARNING_PER_SHARE),
          entry("Depreciation",QuarterlyFilingsHeads.DEPRECIATION),
          entry("EPS",QuarterlyFilingsHeads.EPS),
          entry("Equity_Cap",QuarterlyFilingsHeads.EQUITY),
          entry("Equity Capital",QuarterlyFilingsHeads.EQUITY),
          entry("Expenditure",QuarterlyFilingsHeads.EXPENDITURE),
          entry("Operating Expenses",QuarterlyFilingsHeads.EXPENDITURE),
          entry("Gross Profit",QuarterlyFilingsHeads.GROSS_PROFIT),
          entry("Gross_Profit",QuarterlyFilingsHeads.GROSS_PROFIT),
          entry("Interest",QuarterlyFilingsHeads.INTEREST),
          entry("Interest Earned",QuarterlyFilingsHeads.INTEREST),
          entry("Net Profit",QuarterlyFilingsHeads.NET_PROFIT),
          entry("Net_Profit",QuarterlyFilingsHeads.NET_PROFIT),
          entry("Net Profit Margin",QuarterlyFilingsHeads.NET_PROFIT_MARGIN),
          entry("Nos. of Shares - Non Promoters",QuarterlyFilingsHeads.NON_PROMOTER_SHARES),
          entry("NS_IEOI",QuarterlyFilingsHeads.NS_IEOI),
          entry("Operating Profit",QuarterlyFilingsHeads.OPERATING_PROFIT),
          entry("Operating_Profit",QuarterlyFilingsHeads.OPERATING_PROFIT),
          entry("Operating Profit Margin",QuarterlyFilingsHeads.OPERATING_PROFIT_MARGIN),
          entry("Other Income",QuarterlyFilingsHeads.OTHER_INCOME),
          entry("Other_Income",QuarterlyFilingsHeads.OTHER_INCOME),
          entry("PAT",QuarterlyFilingsHeads.PAT),
          entry("PBT",QuarterlyFilingsHeads.PBT),
          entry("Percent of Shares - Non Promoters",QuarterlyFilingsHeads.NON_PROMOTER_SHARES),
          entry("Profit after Tax",QuarterlyFilingsHeads.PAT),
          entry("Profit before Tax",QuarterlyFilingsHeads.PBT),
          entry("Prom_No_Of_Shares",QuarterlyFilingsHeads.PROMOTER_SHARES),
          entry("Prom_Percent_Of_Shares",QuarterlyFilingsHeads.PROMOTER_PERCENTAGE_OF_SHARES),
          entry("Reserves",QuarterlyFilingsHeads.RESERVES),
          entry("Result Type",QuarterlyFilingsHeads.RESULT_TYPE),
          entry("Share of Profit & Loss of Asso",QuarterlyFilingsHeads.PROFIT_AND_LOSS_OF_ASSOCIATES),
          entry("Share of profit(loss) of associates and joint ventures",QuarterlyFilingsHeads.PROFIT_AND_LOSS_OF_ASSOCIATES),
          entry("Tax",QuarterlyFilingsHeads.TAX),
          entry("TAX",QuarterlyFilingsHeads.TAX),
          entry("Total Income",QuarterlyFilingsHeads.TOTAL_INCOME),
          entry("Total_Income",QuarterlyFilingsHeads.TOTAL_INCOME),
          entry("Provisions and Contingencies", QuarterlyFilingsHeads.PROVISIONS_CONTINGENCIES),
          entry("Type", QuarterlyFilingsHeads.TYPE),
          entry("Period Ending", QuarterlyFilingsHeads.PERIOD_ENDING),
          entry("Date Begin", QuarterlyFilingsHeads.PERIOD_BEGINNING),
          entry("Date End", QuarterlyFilingsHeads.PERIOD_ENDING),
          entry("No. of Months", QuarterlyFilingsHeads.NO_OF_MONTHS)

  );
  public Optional<QuarterlyFilingsHeads> get(String head) {
      return Optional.ofNullable(headDictionary.get(head));
  }
}
