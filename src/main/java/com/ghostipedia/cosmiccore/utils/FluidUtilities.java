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

    /**
     * Gets the still texture sprite for a given fluid.
     *
     * @param fluid The fluid to retrieve the still texture from.
     * @return The {@link TextureAtlasSprite} for the fluid's still texture.
     */
    @SuppressWarnings("deprecation")
    public static TextureAtlasSprite getStillSprite(Fluid fluid) {
        var clientFluid = IClientFluidTypeExtensions.of(fluid);
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        return atlas.apply(clientFluid.getStillTexture());
    }

    /**
     * Gets the flowing texture sprite for a given fluid.
     *
     * @param fluid The fluid to retrieve the flowing texture from.
     * @return The {@link TextureAtlasSprite} for the fluid's flowing texture.
     */
    @SuppressWarnings("deprecation")
    public static TextureAtlasSprite getFlowingSprite(Fluid fluid) {
        var clientFluid = IClientFluidTypeExtensions.of(fluid);
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        return atlas.apply(clientFluid.getFlowingTexture());
    }

}
