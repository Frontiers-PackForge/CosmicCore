package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.ghostipedia.cosmiccore.integration.emi.MultiblockPreviewSchemaCache;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.MultiblockInfoEmiCategory.MultiblockInfoEmiWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(value = MultiblockInfoEmiWrapper.class, remap = false)
public class MultiblockInfoEmiWrapperSchemaCacheMixin {

    @Shadow
    @Final
    private MultiblockMachineDefinition definition;

    @Redirect(method = "initializeContainedBlocks",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/multiblock/util/AbstractStructureHelper;populate(Ljava/util/Map;Lcom/gregtechceu/gtceu/api/multiblock/pattern/IBlockPattern;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;Lnet/minecraft/core/Direction;Lnet/minecraft/core/Direction;Z)V"),
              remap = false)
    private void cosmiccore$capturePreparedSchema(AbstractStructureHelper helper,
                                                  Map<BlockPos, BlockInfo> resultStructure,
                                                  IBlockPattern pattern,
                                                  Long2ObjectMap<BlockInfo> userBlockPreferences,
                                                  Direction frontFacing,
                                                  Direction upFacing,
                                                  boolean isFlipped) {
        helper.populate(resultStructure, pattern, userBlockPreferences, frontFacing, upFacing, isFlipped);
        MultiblockPreviewSchemaCache.capture(this.definition, resultStructure);
    }
}
