package com.ghostipedia.cosmiccore.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import thedarkcolour.gendustry.registry.GItems;

@EmiEntrypoint
public class CosmicCoreEMIPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.setDefaultComparison(GItems.GENE_SAMPLE.item(), Comparison.compareNbt());
    }
}
