package com.ghostipedia.cosmiccore.mixin.embers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Embers Reignited places its lead/silver ore from code, via {@code EmbersLateWorldgen}'s per-chunk retrogen pass
 * ({@code placeMissingOreFeatures} calling the hardcoded {@code EmbersConfiguredFeatures.ORE_*}). Because it runs
 * off in-code ConfiguredFeature objects, no datapack override (kubejs/data, mod data, biome modifier, no_op
 * feature) can stop it. CosmicFrontiers bundles those ores into its own veins, so this cancels the ore retrogen
 * while leaving {@code populateChunk}'s chunk bookkeeping intact (cancelling the leaf call, not the entry point,
 * avoids re-queuing the chunk every tick). require=0 so an Embers update that renames the method degrades to a
 * warning instead of crashing the game (Embers 1.5.4 dropped the old ruin methods this way).
 */
@Mixin(targets = "com.rekindled.embers.worldgen.EmbersLateWorldgen", remap = false)
public class EmbersLateWorldgenMixin {

    @Inject(method = "placeMissingOreFeatures", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void cosmiccore$noEmbersOreRetrogen(CallbackInfo ci) {
        ci.cancel();
    }
}
