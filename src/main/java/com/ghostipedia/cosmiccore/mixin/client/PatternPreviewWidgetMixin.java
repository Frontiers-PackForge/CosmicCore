package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.mixin.accessor.MBPatternAccessor;
import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to fix NPE crash when clicking on blocks in EMI multiblock preview
 * when the pattern uses predicates that accept air (for tiered multiblocks).
 *
 * The crash occurs because patterns[index].predicateMap is null when the
 * pattern's checkPatternAt() fails during preview generation (which happens
 * when tier-detecting predicates accept air instead of actual blocks).
 */
@Mixin(value = PatternPreviewWidget.class, remap = false)
public abstract class PatternPreviewWidgetMixin {

    @Shadow
    @Final
    public PatternPreviewWidget.MBPattern[] patterns;

    @Shadow
    private int index;

    /**
     * Inject at the head of onPosSelected to bail out early if predicateMap is null.
     * This prevents the NPE crash while still allowing the preview to render.
     */
    @Inject(method = "onPosSelected", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$preventNullPredicateMapCrash(BlockPos pos, Direction facing, CallbackInfo ci) {
        if (patterns != null && index >= 0 && index < patterns.length) {
            PatternPreviewWidget.MBPattern pattern = patterns[index];
            if (pattern != null) {
                // Use accessor to check if predicateMap is null
                var predicateMap = ((MBPatternAccessor) (Object) pattern).cosmiccore$getPredicateMap();
                if (predicateMap == null) {
                    // predicateMap is null, bail out to prevent NPE
                    ci.cancel();
                }
            }
        }
    }
}
