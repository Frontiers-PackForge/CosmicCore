package com.ghostipedia.cosmiccore.mixin.adastra;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.systems.GravityApiImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Optimizes Ad Astra's fluid hooks by short-circuiting gravity checks
 * for vanilla dimensions where these calculations are unnecessary.
 *
 * Ad Astra hooks into FlowingFluid to check gravity on every fluid spread,
 * which is extremely expensive during worldgen. This mixin bypasses those checks
 * for dimensions that don't need planet physics, returning normal Earth gravity (1.0f).
 */
@Mixin(value = GravityApiImpl.class, remap = false)
public class AdAstraGravityOptimizationMixin {

    @Unique
    private static final Set<ResourceKey<Level>> cosmiccore$VANILLA_DIMENSIONS = Set.of(
            Level.OVERWORLD,
            Level.NETHER,
            Level.END);

    @Inject(method = "getGravity(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$skipGravityCheckForVanillaDimensions(Level level, BlockPos pos,
                                                                 CallbackInfoReturnable<Float> cir) {
        if (level != null && cosmiccore$VANILLA_DIMENSIONS.contains(level.dimension())) {
            cir.setReturnValue(1.0f);
        }
    }
}
