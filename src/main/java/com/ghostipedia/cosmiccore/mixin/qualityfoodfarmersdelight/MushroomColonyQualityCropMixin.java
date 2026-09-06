package com.ghostipedia.cosmiccore.mixin.qualityfoodfarmersdelight;

import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.cadentem.quality_food.util.QualityUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;

@Mixin(value = QualityUtils.class, remap = false)
public abstract class MushroomColonyQualityCropMixin {

    @ModifyReturnValue(
                       method = "isRelevantCrop(Lnet/minecraft/world/level/block/state/BlockState;)Z",
                       at = @At("RETURN"),
                       remap = false)
    private static boolean cosmiccore$harvestMatureMushroomColony(boolean relevant, BlockState state) {
        if (relevant || !(state.getBlock() instanceof MushroomColonyBlock colony)) {
            return relevant;
        }
        return state.getValue(colony.getAgeProperty()) == colony.getMaxAge();
    }
}
