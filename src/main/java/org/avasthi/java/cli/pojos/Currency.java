package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record Currency(UUID id, Date date, float usd, float gbp, float euro, float yen) {
}
