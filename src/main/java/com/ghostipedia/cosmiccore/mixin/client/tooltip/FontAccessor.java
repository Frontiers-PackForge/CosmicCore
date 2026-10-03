package com.ghostipedia.cosmiccore.mixin.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface FontAccessor {

    @Accessor("filterFishyGlyphs")
    boolean cosmiccore$getFilterFishyGlyphs();

    @Invoker("getFontSet")
    FontSet cosmiccore$invokeGetFontSet(ResourceLocation fontLocation);
}
