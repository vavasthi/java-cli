package org.avasthi.java.cli;

import com.google.gson.JsonElement;

import java.util.Map;

public record CPUUtilization(String account,
                             String algorithm,
                             String region,
                             String resourceGroup,
                             String subscription,
                             String account2,
                             String scope,
                             String uuid,
                             String region2,
                             String algorithm2,
                             Data data) {
}
