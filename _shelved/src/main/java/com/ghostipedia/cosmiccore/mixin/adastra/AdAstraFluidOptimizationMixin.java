package com.ghostipedia.cosmiccore.mixin.adastra;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.systems.TemperatureApiImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Optimizes Ad Astra's fluid hooks by short-circuiting temperature checks
 * for vanilla dimensions where these calculations are unnecessary.
 *
 * Ad Astra hooks into FlowingFluid to check temperature on every fluid tick,
 * which is extremely expensive during worldgen. This mixin bypasses those checks
 * for dimensions that don't need planet physics.
 */
@Mixin(value = TemperatureApiImpl.class, remap = false)
public class AdAstraFluidOptimizationMixin {

    @Unique
    private static final Set<ResourceKey<Level>> cosmiccore$VANILLA_DIMENSIONS = Set.of(
            Level.OVERWORLD,
            Level.NETHER,
            Level.END);

    @Inject(method = "isLiveable(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$skipLiveableCheckForVanillaDimensions(Level level, BlockPos pos,
                                                                  CallbackInfoReturnable<Boolean> cir) {
        if (level != null && cosmiccore$VANILLA_DIMENSIONS.contains(level.dimension())) {
            cir.setReturnValue(true);
        }
    }
}
