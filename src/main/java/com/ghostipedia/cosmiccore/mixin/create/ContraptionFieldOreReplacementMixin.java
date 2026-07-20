package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldBlockRules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import com.simibubi.create.content.contraptions.Contraption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Contraption.class)
public abstract class ContraptionFieldOreReplacementMixin {

    @Redirect(
              method = "addBlocksToWorld",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                       ordinal = 0))
    private boolean cosmiccore$voidReplacedFieldOre(Level level, BlockPos pos, boolean dropBlock) {
        boolean shouldDrop = dropBlock && !OreFieldBlockRules.isFieldOre(level.getBlockState(pos));
        return level.destroyBlock(pos, shouldDrop);
    }
}
