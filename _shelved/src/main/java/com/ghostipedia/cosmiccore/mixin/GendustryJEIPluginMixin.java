package com.ghostipedia.cosmiccore.mixin;

import mezz.jei.api.registration.ISubtypeRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import thedarkcolour.gendustry.compat.jei.GendustryJeiPlugin;
import thedarkcolour.gendustry.registry.GItems;

@Mixin(value = GendustryJeiPlugin.class, remap = false)
public class GendustryJEIPluginMixin {

    /**
     * @author - Ghostipedia
     * @reason - TMRV Visual Compat for CosmicCore and Frontiers, the subtypeInterpreter doesn't work.
     */
    @Overwrite
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(GItems.GENE_SAMPLE.asItem());
    }
}
