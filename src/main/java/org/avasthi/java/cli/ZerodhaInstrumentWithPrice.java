package org.avasthi.java.cli;

import com.zerodhatech.models.Tick;
import lombok.Builder;
import lombok.Data;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;

@Data
@Builder
public class ZerodhaInstrumentWithPrice {
    private ZerodhaInstrument zerodhaInstrument;
    private Tick tick;
}
