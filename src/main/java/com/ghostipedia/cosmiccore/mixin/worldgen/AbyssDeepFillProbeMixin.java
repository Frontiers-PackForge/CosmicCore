package com.ghostipedia.cosmiccore.mixin.worldgen;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NoiseChunk.class, priority = 100)
public class AbyssDeepFillProbeMixin {

    @Unique
    private int cosmiccore$curY = Integer.MAX_VALUE;

    @Inject(method = "updateForY", at = @At("HEAD"))
    private void cosmiccore$captureY(int cellEndBlockY, double y, CallbackInfo ci) {
        cosmiccore$curY = cellEndBlockY;
    }

    @Inject(method = "getInterpolatedState", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$skipDeepWater(CallbackInfoReturnable<BlockState> cir) {
        if (cosmiccore$curY > -700 && cosmiccore$curY < -64) {
            cir.setReturnValue(Blocks.WATER.defaultBlockState());
        }
    }
}
