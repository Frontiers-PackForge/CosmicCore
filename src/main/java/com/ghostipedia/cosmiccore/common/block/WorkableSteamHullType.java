package com.ghostipedia.cosmiccore.common.block;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public record WorkableSteamHullType(String name, ResourceLocation bottom, ResourceLocation top, ResourceLocation side) {

    public static WorkableSteamHullType BRONZE_BRICK_HULL = new WorkableSteamHullType("bricked_bronze",
            GTCEu.id("block/casings/steam/bricked_bronze/bottom"),
            GTCEu.id("block/casings/steam/bricked_bronze/top"),
            GTCEu.id("block/casings/steam/bricked_bronze/side"));
    public static WorkableSteamHullType STEEL_BRICK_HULL = new WorkableSteamHullType("bricked_steel",
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
            GTCEu.id("block/casings/firebox/machine_casing_firebox_steel"));

    @NotNull
    @Override
    public String toString() {
        return name();
    }
}
