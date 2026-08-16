package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import com.gregtechceu.gtceu.api.multiblock.pattern.BasicSliceStrategy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = BasicSliceStrategy.class, remap = false)
public interface BasicSliceStrategyAccessor {

    @Accessor("multiblockSlices")
    List<?> cosmiccore$getMultiblockSlices();
}
