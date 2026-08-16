package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.recipe.PowerRecipeVoltageConverter;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = GTRecipe.class, remap = false)
public abstract class GTRecipeVoltageStripper {

    @Shadow
    @Final
    private AtomicReference<Object> inputEUt;

    @Inject(method = "setId(Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("TAIL"),
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$applyPowerRecipeWorkload(ResourceLocation id, CallbackInfo ci) {
        if (PowerRecipeVoltageConverter.apply((GTRecipe) (Object) this)) {
            inputEUt.set(null);
        }
    }
}
