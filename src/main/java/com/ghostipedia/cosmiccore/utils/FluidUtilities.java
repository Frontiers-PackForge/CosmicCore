package com.ghostipedia.cosmiccore.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.registries.BuiltInRegistries;

public class FluidUtilities {

    public static Fluid getFluid(String fluidResloc) {
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidResloc));
        return fluid == null ? Fluids.EMPTY : fluid;
    }
}
