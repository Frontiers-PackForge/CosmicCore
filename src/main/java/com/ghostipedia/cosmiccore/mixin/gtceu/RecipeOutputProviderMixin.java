package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeOutputProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeOutputProvider.class, remap = false)
public class RecipeOutputProviderMixin {

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void cosmicCore$skipStellarModule(CompoundTag data, RecipeLogic capability, CallbackInfo ci) {
        if (capability.getMachine() instanceof StellarBaseModule) {
            ci.cancel();
        }
    }
}
