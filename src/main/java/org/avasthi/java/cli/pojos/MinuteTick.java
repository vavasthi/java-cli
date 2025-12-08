package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record MinuteTick(UUID id, String symbol, Date timestmap, float open, float high, float low, float close, float volume) {
}
