package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.ghostipedia.cosmiccore.integration.emi.TrackedStructureMap;

import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.MultiblockInfoEmiCategory.MultiblockInfoEmiWrapper;

import net.minecraft.core.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(value = MultiblockInfoEmiWrapper.class, remap = false)
public class MultiblockInfoEmiWrapperCountCacheMixin {

    @ModifyVariable(method = "initializeContainedBlocks", at = @At("STORE"), ordinal = 0, remap = false)
    private Map<BlockPos, BlockInfo> cosmiccore$trackStructureCounts(Map<BlockPos, BlockInfo> original) {
        return new TrackedStructureMap(original);
    }
}
