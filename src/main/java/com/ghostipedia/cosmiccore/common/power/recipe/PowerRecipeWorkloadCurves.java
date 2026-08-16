package com.ghostipedia.cosmiccore.common.power.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class PowerRecipeWorkloadCurves {
    //Policy Rate File that determines amperage bands based on voltage, it's not exactly the best option but a million static fields basically would have been the same anyway...
    private static final String POLICY_RESOURCE = "/data/cosmiccore/power_recipe_tweaks.json";
    private static final int POLICY_SCHEMA = 1;
    private static final Map<String, Curve> CURVES = load();

    private PowerRecipeWorkloadCurves() {}

    public static int targetAmperage(String recipeMap, double workScore) {
        Curve curve = CURVES.get(recipeMap);
        return curve == null ? 0 : curve.targetAmperage(workScore);
    }

    static int size() {
        return CURVES.size();
    }

    private static Map<String, Curve> load() {
        try (InputStream stream = PowerRecipeWorkloadCurves.class.getResourceAsStream(POLICY_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Missing power recipe workload policy " + POLICY_RESOURCE);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.get("schema").getAsInt() != POLICY_SCHEMA) {
                    throw new IllegalStateException("Unsupported power recipe workload policy schema");
                }
                Map<String, Curve> curves = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("maps").entrySet()) {
                    Curve curve = getCurve(entry);
                    if (!curve.valid() || curves.put(entry.getKey(), curve) != null) {
                        throw new IllegalStateException("Invalid power recipe workload curve " + entry.getKey());
                    }
                }
                return Map.copyOf(curves);
            }
        } catch (IOException | RuntimeException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static @NotNull Curve getCurve(Map.Entry<String, JsonElement> entry) {
        JsonObject value = entry.getValue().getAsJsonObject();
        JsonArray thresholds = value.getAsJsonArray("thresholds");
        JsonArray amps = value.getAsJsonArray("amps");
        if (thresholds.size() != 2 || amps.size() != 3) {
            throw new IllegalStateException("Invalid power recipe workload curve " + entry.getKey());
        }
        Curve curve = new Curve(
                thresholds.get(0).getAsDouble(),
                thresholds.get(1).getAsDouble(),
                amps.get(0).getAsInt(),
                amps.get(1).getAsInt(),
                amps.get(2).getAsInt());
        return curve;
    }

    private record Curve(double lowerThreshold, double upperThreshold, int lowerAmps, int middleAmps,
                         int upperAmps) {

        private boolean valid() {
            return Double.isFinite(lowerThreshold) && Double.isFinite(upperThreshold) && lowerThreshold >= 0 &&
                    upperThreshold >= lowerThreshold && lowerAmps > 0 && middleAmps >= lowerAmps &&
                    upperAmps >= middleAmps;
        }

        private int targetAmperage(double workScore) {
            if (!Double.isFinite(workScore) || workScore < 0) {
                return 0;
            }
            if (workScore <= lowerThreshold) {
                return lowerAmps;
            }
            if (workScore <= upperThreshold) {
                return middleAmps;
            }
            return upperAmps;
        }
    }
}
