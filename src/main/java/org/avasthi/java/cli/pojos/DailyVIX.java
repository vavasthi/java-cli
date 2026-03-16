package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record DailyVIX(UUID vixId, Date date, float open, float high, float low, float close, float prevClose, float change, float percentageChange) {
}
