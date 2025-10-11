package com.ghostipedia.cosmiccore.integration.recipes;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import thedarkcolour.gendustry.registry.GItems;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@JeiPlugin
public class CosmicCoreJEIPlugin implements IModPlugin {

    @Getter
    private static IJeiRuntime runtime = null;

    @Override
    public ResourceLocation getPluginUid() {
        return CosmicCore.id("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if (GTCEu.Mods.isREILoaded() || GTCEu.Mods.isEMILoaded()) return;
        registration.useNbtForSubtypes(GItems.GENE_SAMPLE.asItem());
    }
}
