package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record StockMaster(UUID _id, String symbol, String name, String series, Date dateOfListing, float paidUpValue, int marketLot, String isin, float faceValue) {
}
