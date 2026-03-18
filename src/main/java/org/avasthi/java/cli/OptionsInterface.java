package org.avasthi.java.cli;

import org.avasthi.java.cli.pojos.OptionPair;
import org.avasthi.java.cli.pojos.ZerodhaInstrument;

import java.util.List;
import java.util.Set;

public interface OptionsInterface {
    default OptionPair getOptionPair(float strike) {

        throw new RuntimeException("Not applicable for this class..");
    }
    List<Long> getTokensToSubscribe();
    long getToken();
    double getLastPrice();
    void subscribe();
    default Set<Float> findNearestStrike(double spotPrice) {
        throw new RuntimeException("Not applicable for this class..");
    }
    default Set<ZerodhaInstrument> getallOptions() {
        throw new RuntimeException("Not applicable for this class..");
    }
    default String getAsset() {
        throw new RuntimeException("Not applicable for this class..");
    }

}
