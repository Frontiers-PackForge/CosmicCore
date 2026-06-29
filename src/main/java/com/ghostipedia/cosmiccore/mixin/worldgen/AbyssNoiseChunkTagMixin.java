package com.ghostipedia.cosmiccore.mixin.worldgen;

import com.ghostipedia.cosmiccore.common.data.worldgen.abyss.AbyssNoiseChunk;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.CosmicFloodedNoiseChunkGenerator;

import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class AbyssNoiseChunkTagMixin {

    @Inject(method = "createNoiseChunk", at = @At("RETURN"), require = 0)
    private void cosmiccore$tagAbyss(CallbackInfoReturnable<NoiseChunk> cir) {
        if ((Object) this instanceof CosmicFloodedNoiseChunkGenerator) {
            NoiseChunk noiseChunk = cir.getReturnValue();
            if (noiseChunk instanceof AbyssNoiseChunk abyss) {
                abyss.cosmiccore$setAbyss(true);
            }
        }
    }
}
