package com.ghostipedia.cosmiccore.api.item.component;

import net.minecraft.resources.ResourceLocation;

import java.util.function.IntSupplier;

public record HaloItemRenderData(int size, IntSupplier color, ResourceLocation texture, boolean drawHalo,
                                 boolean drawPulse)
        implements ICustomRenderer {

    public HaloItemRenderData(int size, int color, ResourceLocation texture, boolean drawHalo, boolean drawPulse) {
        this(size, () -> color, texture, drawHalo, drawPulse);
    }
}
