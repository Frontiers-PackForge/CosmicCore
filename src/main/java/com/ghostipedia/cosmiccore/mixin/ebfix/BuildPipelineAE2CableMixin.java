package com.ghostipedia.cosmiccore.mixin.ebfix;

import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingAE2CableCompat;

import net.minecraft.world.item.ItemStack;

import neoforge.nl.requios.effortlessbuilding.buildpipeline.BuildPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BuildPipeline.class)
public abstract class BuildPipelineAE2CableMixin {

    @Inject(method = "isBuildTriggerItem", at = @At("RETURN"), cancellable = true)
    private static void cosmiccore$acceptAE2CenterCables(
                                                         ItemStack stack,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && EffortlessBuildingAE2CableCompat.isCableItem(stack)) {
            cir.setReturnValue(true);
        }
    }
}
