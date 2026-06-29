package com.ghostipedia.cosmiccore.mixin.worldgen;

import com.ghostipedia.cosmiccore.common.data.worldgen.abyss.AbyssNoiseChunk;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunk.class)
public class AbyssDeepWaterMixin implements AbyssNoiseChunk {

    @Unique
    private static final int COSMICCORE$BAND_TOP = -64;

    @Unique
    private static final int COSMICCORE$BAND_FLOOR = -730;

    @Unique
    private boolean cosmiccore$abyss = false;

    @Unique
    private int cosmiccore$y = Integer.MAX_VALUE;

    @Override
    public void cosmiccore$setAbyss(boolean abyss) {
        this.cosmiccore$abyss = abyss;
    }

    @Override
    public boolean cosmiccore$isAbyss() {
        return this.cosmiccore$abyss;
    }

    @Inject(method = "updateForY", at = @At("HEAD"))
    private void cosmiccore$captureY(int cellEndBlockY, double y, CallbackInfo ci) {
        this.cosmiccore$y = cellEndBlockY;
    }

    @Inject(method = "getInterpolatedState", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$skipDeepWater(CallbackInfoReturnable<BlockState> cir) {
        if (this.cosmiccore$abyss && this.cosmiccore$y > COSMICCORE$BAND_FLOOR &&
                this.cosmiccore$y < COSMICCORE$BAND_TOP) {
            cir.setReturnValue(Blocks.WATER.defaultBlockState());
        }
    }
}
