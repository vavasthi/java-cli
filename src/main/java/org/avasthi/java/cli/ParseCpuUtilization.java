package org.avasthi.java.cli;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ParseCpuUtilization {

    public static void main(String[] args) throws IOException {
        ParseCpuUtilization pcu = new ParseCpuUtilization();
        File jsonFile = new File("cpu.json");
        System.out.println(jsonFile.getAbsolutePath());
        String json = Files.readString(jsonFile.toPath());
        pcu.parseJson(json);
    }
    private List<CPUUtilization> parseJson(String json) {

        List<CPUUtilization> output = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        JsonObject ts = gson.fromJson(json, JsonObject.class);
        for (Map.Entry<String, JsonElement> e1 : ts.entrySet()) {

            String account = e1.getKey();
            for (Map.Entry<String, JsonElement> e2 : e1.getValue().getAsJsonObject().entrySet()) {
                String algorithm = e2.getKey();
                for (Map.Entry<String, JsonElement> e3 : e2.getValue().getAsJsonObject().entrySet()) {
                    String region = e3.getKey();
                    for (Map.Entry<String, JsonElement> e4 : e3.getValue().getAsJsonObject().entrySet()) {
                        String resourceGroup = e4.getKey();
                        for (Map.Entry<String, JsonElement> e5 : e4.getValue().getAsJsonObject().entrySet()) {
                            String subscription = e5.getKey();
                            for (Map.Entry<String, JsonElement> e6 : e5.getValue().getAsJsonObject().entrySet()) {
                                String account2 = e6.getKey();
                                for (Map.Entry<String, JsonElement> e7 : e6.getValue().getAsJsonObject().entrySet()) {
                                    String scope = e7.getKey();
                                    for (Map.Entry<String, JsonElement> e8 : e7.getValue().getAsJsonObject().entrySet()) {
                                        String uuid = e8.getKey();
                                        for (Map.Entry<String, JsonElement> e9 : e8.getValue().getAsJsonObject().entrySet()) {
                                            String region2 = e9.getKey();
                                            for (Map.Entry<String, JsonElement> e10 : e9.getValue().getAsJsonObject().entrySet()) {
                                                String algorithm2 = e10.getKey();
                                                Data data = gson.fromJson(e10.getValue().toString(), Data.class);
                                                CPUUtilization utilization = new CPUUtilization(account,
                                                        algorithm,
                                                        region,
                                                        resourceGroup,
                                                        subscription,
                                                        account2,
                                                        scope,
                                                        uuid,
                                                        region2,
                                                        algorithm2,
                                                        data);
                                                output.add(utilization);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        return output;
    }
}
