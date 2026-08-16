package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.gregtechceu.gtceu.api.multiblock.pattern.BasicSliceStrategy$MultiblockSlice", remap = false)
public interface BasicSliceGroupAccessor {

    @Accessor("minRepeats")
    int cosmiccore$getMinRepeats();

    @Accessor("maxRepeats")
    int cosmiccore$getMaxRepeats();

    @Accessor("startInclusive")
    int cosmiccore$getStartInclusive();

    @Accessor("endExclusive")
    int cosmiccore$getEndExclusive();
}
