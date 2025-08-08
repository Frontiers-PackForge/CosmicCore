package com.ghostipedia.cosmiccore.mixin.accessor;

import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MultiblockState.class, remap = false)
public interface IMultiblockStateAccessor {

    @Invoker("clean")
    void clean();

    @Invoker("update")
    boolean update(BlockPos posIn, TraceabilityPredicate predicate);
}
