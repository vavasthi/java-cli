package org.avasthi.java.cli;

import java.util.Date;
import java.util.Map;

public record Data(Map<Date, Float> datapoints, Float[] rawValues) {
}
