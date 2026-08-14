package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.TwelvefoldConductorRegistration;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTMaterials.class, remap = false)
public abstract class GTMaterialsTwelvefoldConductorMixin {

    @Inject(method = "init()V", at = @At("HEAD"), require = 1)
    private static void cosmiccore$registerTwelvefoldConductors(CallbackInfo ci) {
        TwelvefoldConductorRegistration.registerPrefixesAndInsulations();
    }

    @Inject(method = "init()V", at = @At("TAIL"), require = 1)
    private static void cosmiccore$attachTwelvefoldInsulationMaterial(CallbackInfo ci) {
        TwelvefoldConductorRegistration.attachRubberMaterial();
    }
}
