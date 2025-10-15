package org.avasthi.java.cli.pojos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;

public record QuarterlyReportTable(String audited,
                                   String bank,
                                   LocalDateTime broadCastDate,
                                   String companyName,
                                   String consolidated,
                                   String cumulative,
                                   String difference,
                                   LocalDateTime exchdisstime,
                                   LocalDateTime filingDate,
                                   LocalDate financialYearStart,
                                   LocalDate financialYearEnd,
                                   QuarterlyReportFormatType format,
                                   LocalDate fromDate,
                                   QuarterlyReportIndAs indAs,
                                   String industry,
                                   String isin,
                                   QuarterlyReportOldNewFlag oldNewFlag,
                                   String params,
                                   QuarterlyReportPeriod period,
                                   String reInd,
                                   String relatingTo,
                                   String resultDescription,
                                   String resultDetailedDataLink,
                                   String seqNumber,
                                   String symbol,
                                   LocalDate toDate,
                                   String xbrl) {

  private static DateTimeFormatter ddmmmyyyyhhmmss = DateTimeFormatter.ofPattern("dd-MMM-yyyy kk:mm[:ss]");
  private static DateTimeFormatter ddmmmyyyy = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

  public QuarterlyReportTable(String audited,
                              String bank,
                              String broadCastDate,
                              String companyName,
                              String consolidated,
                              String cumulative,
                              String difference,
                              String exchdisstime,
                              String filingDate,
                              String format,
                              String fromDate,
                              String indAs,
                              String industry,
                              String isin,
                              String oldNewFlag,
                              String params,
                              String period,
                              String reInd,
                              String relatingTo,
                              String resultDescription,
                              String resultDetailedDataLink,
                              String seqNumber,
                              String symbol,
                              String toDate,
                              String xbrl) throws ParseException {
    this( audited,
            bank,
            LocalDateTime.parse(broadCastDate, ddmmmyyyyhhmmss),
            companyName,
            consolidated,
            cumulative,
            difference,
            LocalDateTime.parse(exchdisstime, ddmmmyyyyhhmmss),
            LocalDateTime.parse(filingDate, ddmmmyyyyhhmmss),
            startOfFinancialYear(fromDate),
            endOfFinancialYear(toDate),
            QuarterlyReportFormatType.fromDescription(format),
            LocalDate.parse(fromDate, ddmmmyyyy),
            QuarterlyReportIndAs.fromDescription(indAs),
            industry,
            isin,
            QuarterlyReportOldNewFlag.fromDescription(oldNewFlag),
            params,
            QuarterlyReportPeriod.fromDescription(period),
            reInd,
            relatingTo,
            resultDescription,
            resultDetailedDataLink,
            seqNumber,
            symbol,
            LocalDate.parse(toDate, ddmmmyyyy),
            xbrl);
  }
  private static LocalDate startOfFinancialYear(String date) throws ParseException {
    LocalDate d = LocalDate.parse(date, ddmmmyyyy);

    while (d.getDayOfMonth() != 1 && d.getMonthValue() != 4) {
      d = d.minusDays(1);
    }
    return d;
  }
  private static LocalDate endOfFinancialYear(String date) throws ParseException {
    LocalDate d = LocalDate.parse(date, ddmmmyyyy);

    while (d.getDayOfMonth() != 1 && d.getMonthValue() != 4) {
      d = d.plusDays(1);
    }
    d = d.minusDays(1);
    return d;
  }
}
