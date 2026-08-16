package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.multiblock.GroupedSlicePreviewSupport;

import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.BlockPatternHelper;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockPatternHelper.class, remap = false)
public class GroupedSliceBlockPatternHelperMixin {

    @Shadow
    @Final
    private Int2IntMap sliceRepeats;

    @Inject(method = "flattenBlockPattern", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$flattenGroupedSlices(BlockPattern pattern, CallbackInfoReturnable<char[][][]> cir) {
        char[][][] flattened = GroupedSlicePreviewSupport.flatten(pattern, sliceRepeats);
        if (flattened != null) cir.setReturnValue(flattened);
    }
}
