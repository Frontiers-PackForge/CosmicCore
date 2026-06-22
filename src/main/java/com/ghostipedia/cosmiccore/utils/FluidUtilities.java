package com.ghostipedia.cosmiccore.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidUtilities {

    public static Fluid getFluid(String fluidResloc) {
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidResloc));
        return fluid == null ? Fluids.EMPTY : fluid;
    }
}
