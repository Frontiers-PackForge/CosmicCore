package com.ghostipedia.cosmiccore.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidUtilities {

    public static Fluid getFluid(String fluidResloc) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidResloc));
        return fluid == null ? Fluids.EMPTY : fluid;
    }

    @SuppressWarnings("deprecation")
    public static TextureAtlasSprite getStillSprite(Fluid fluid) {
        FluidStack fluidStack = new FluidStack(fluid, 1);
        return  Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(IClientFluidTypeExtensions.of(fluid).getStillTexture(fluidStack));
    }

    @SuppressWarnings("deprecation")
    public static TextureAtlasSprite getFlowingSprite(Fluid fluid) {
        FluidStack fluidStack = new FluidStack(fluid, 1);
        return  Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(IClientFluidTypeExtensions.of(fluid).getFlowingTexture(fluidStack));
    }

}
