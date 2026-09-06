package com.ghostipedia.cosmiccore.mixin.qualityfoodfarmersdelight;

import net.minecraft.world.level.block.Block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.cadentem.quality_food.util.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.MushroomColonyBlock;

@Mixin(value = Utils.class, remap = false)
public abstract class MushroomColonyQualityCarrierMixin {

    @ModifyReturnValue(
                       method = "isValidBlock(Lnet/minecraft/world/level/block/Block;)Z",
                       at = @At("RETURN"),
                       remap = false)
    private static boolean cosmiccore$carryMushroomColonyQuality(boolean valid, Block block) {
        return valid || block instanceof MushroomColonyBlock;
    }
}
