package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.ghostipedia.cosmiccore.integration.emi.TrackedStructureMap;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = AbstractStructureHelper.class, remap = false)
public class AbstractStructureHelperCountCacheMixin {

    @Inject(method = "countPopulatedGlobal", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$countTrackedGlobal(Map<BlockPos, BlockInfo> resultStructure,
                                                      BasePredicate basePredicate,
                                                      CallbackInfoReturnable<Integer> cir) {
        if (resultStructure instanceof TrackedStructureMap tracked) {
            cir.setReturnValue(tracked.countGlobal(basePredicate));
        }
    }

    @Inject(method = "countPopulatedInLayer", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$countTrackedLayer(Map<BlockPos, BlockInfo> resultStructure,
                                                     BasePredicate basePredicate,
                                                     Direction direction,
                                                     int offset,
                                                     CallbackInfoReturnable<Integer> cir) {
        if (resultStructure instanceof TrackedStructureMap tracked) {
            cir.setReturnValue(tracked.countInLayer(basePredicate, direction.getAxis(), offset));
        }
    }
}
