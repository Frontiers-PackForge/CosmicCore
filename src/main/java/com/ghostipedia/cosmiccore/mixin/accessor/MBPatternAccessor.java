package com.ghostipedia.cosmiccore.mixin.accessor;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Accessor mixin to access package-private fields in MBPattern inner class.
 */
@Mixin(value = PatternPreviewWidget.MBPattern.class, remap = false)
public interface MBPatternAccessor {

    @Accessor("predicateMap")
    Map<BlockPos, TraceabilityPredicate> cosmiccore$getPredicateMap();
}
