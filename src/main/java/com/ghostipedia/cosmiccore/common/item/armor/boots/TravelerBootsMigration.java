package com.ghostipedia.cosmiccore.common.item.armor.boots;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class TravelerBootsMigration {

    private static final Map<ResourceLocation, ResourceLocation> ALIASES = Map.of(
            CosmicCore.id("hydraulic_boots"), CosmicCore.id("steel_travelers_boots"),
            CosmicCore.id("nano_boots"), CosmicCore.id("nano_travelers_boots"),
            CosmicCore.id("quark_boots"), CosmicCore.id("quark_travelers_boots"),
            CosmicCore.id("sanguine_boots"), CosmicCore.id("quark_travelers_boots"),
            CosmicCore.id("shadebloom_boots"), CosmicCore.id("shadebloom_travelers_boots"));

    private TravelerBootsMigration() {}

    public static void registerAliases() {
        ALIASES.forEach(BuiltInRegistries.ITEM::addAlias);
    }
}
