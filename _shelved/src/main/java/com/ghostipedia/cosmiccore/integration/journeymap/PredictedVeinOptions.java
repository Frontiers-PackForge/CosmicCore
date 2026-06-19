package com.ghostipedia.cosmiccore.integration.journeymap;

import com.ghostipedia.cosmiccore.CosmicCore;

import journeymap.client.api.option.BooleanOption;
import journeymap.client.api.option.OptionCategory;

public class PredictedVeinOptions {

    private final BooleanOption predictedVeinsOption;

    public PredictedVeinOptions() {
        final OptionCategory category = new OptionCategory(CosmicCore.MOD_ID, "cosmiccore.journeymap.options");
        predictedVeinsOption = new BooleanOption(
                category,
                "predicted_veins",
                "cosmiccore.journeymap.options.predicted_veins",
                true);
    }

    public boolean showPredictedVeins() {
        return predictedVeinsOption.get();
    }

    public void togglePredictedVeins(boolean active) {
        predictedVeinsOption.set(active);
        if (!active) {
            PredictedVeinRenderer.hideAllMarkers();
        } else {
            PredictedVeinRenderer.showAllMarkers();
        }
    }
}
