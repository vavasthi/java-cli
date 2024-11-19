package org.avasthi.java.cli.pojos;

import java.util.Date;
import java.util.UUID;

public record CorporateEvent(UUID _id,
                             String symbol,
                             Date date,
                             Float bonus,
                             Float dividend) {
}
