package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldDispatcher;

import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.api.data.worldgen.ores.OreGenerator;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Injects CosmicCore's deterministic ore fields into GTCEu's worldgen. GT's own {@code generateMetadata} produces one
 * weighted vein per grid cell; this appends our field members for the same chunk, so GT then caches, prospector-tags,
 * and paints them through its normal pipeline. require=0 so a future GT rename degrades to a warning rather than a
 * crash.
 */
@Mixin(value = OreGenerator.class, remap = false)
public class OreGeneratorMixin {

    @Inject(method = "generateMetadata", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void cosmiccore$injectFieldVeins(WorldGenLevel level, ChunkGenerator chunkGenerator, ChunkPos chunkPos,
                                             CallbackInfoReturnable<List<GeneratedVeinMetadata>> cir) {
        List<GeneratedVeinMetadata> extra = OreFieldDispatcher.membersInChunk(level, chunkGenerator, chunkPos);
        if (extra.isEmpty()) return;
        List<GeneratedVeinMetadata> combined = new ArrayList<>(cir.getReturnValue());
        combined.addAll(extra);
        cir.setReturnValue(combined);
    }
}
