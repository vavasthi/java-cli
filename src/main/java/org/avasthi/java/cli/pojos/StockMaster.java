package org.avasthi.java.cli.pojos;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class StockMaster implements Serializable {

  public StockMaster(UUID id,
                     String symbol,
                     String name,
                     String series,
                     Date dateOfListing,
                     double paidUpValue,
                     int marketLot,
                     String isin,
                     double faceValue,
                     String stockCode) {
    this.id = id;
    this.symbol = symbol;
    this.name = name;
    this.series = series;
    this.dateOfListing = dateOfListing;
    this.paidUpValue = paidUpValue;
    this.marketLot = marketLot;
    this.isin = isin;
    this.faceValue = faceValue;
    this.stockCode = stockCode;
  }
  public StockMaster() {

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSeries() {
    return series;
  }

  public void setSeries(String series) {
    this.series = series;
  }

  public Date getDateOfListing() {
    return dateOfListing;
  }

  public void setDateOfListing(Date dateOfListing) {
    this.dateOfListing = dateOfListing;
  }

  public double getPaidUpValue() {
    return paidUpValue;
  }

  public void setPaidUpValue(double paidUpValue) {
    this.paidUpValue = paidUpValue;
  }

  public int getMarketLot() {
    return marketLot;
  }

  public void setMarketLot(int marketLot) {
    this.marketLot = marketLot;
  }

  public String getIsin() {
    return isin;
  }

  public void setIsin(String isin) {
    this.isin = isin;
  }

  public double getFaceValue() {
    return faceValue;
  }

  public void setFaceValue(double faceValue) {
    this.faceValue = faceValue;
  }

  public String getStockCode() {
    return stockCode;
  }

  public void setStockCode(String stockCode) {
    this.stockCode = stockCode;
  }

  public boolean isNifty50() {
    return nifty50;
  }

  public void setNifty50(boolean nifty50) {
    this.nifty50 = nifty50;
  }

  public boolean isSensex() {
    return sensex;
  }

  public void setSensex(boolean sensex) {
    this.sensex = sensex;
  }

  @Override
  public String toString() {
    return "StockMaster{" +
            "_id=" + id +
            ", symbol='" + symbol + '\'' +
            ", name='" + name + '\'' +
            ", series='" + series + '\'' +
            ", dateOfListing=" + dateOfListing +
            ", paidUpValue=" + paidUpValue +
            ", marketLot=" + marketLot +
            ", isin='" + isin + '\'' +
            ", faceValue=" + faceValue +
            ", stockCode='" + stockCode + '\'' +
            ", nifty50=" + nifty50 +
            ", sensex=" + sensex +
            '}';
  }

  private UUID id;
  private String symbol;
  String name;
  private String series;
  Date dateOfListing;
  private double paidUpValue;
  private int marketLot;
  private String isin;
  private double faceValue;
  private String stockCode;
  private boolean nifty50;
  private boolean sensex;
}
