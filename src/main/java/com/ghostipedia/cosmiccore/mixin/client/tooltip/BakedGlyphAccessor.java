package com.ghostipedia.cosmiccore.mixin.client.tooltip;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedGlyph.class)
public interface BakedGlyphAccessor {

    @Accessor("left")
    float cosmiccore$getLeft();

    @Accessor("right")
    float cosmiccore$getRight();

    @Accessor("up")
    float cosmiccore$getUp();

    @Accessor("down")
    float cosmiccore$getDown();
}
