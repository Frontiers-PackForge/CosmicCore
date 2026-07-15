package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeOutputProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeOutputProvider.class, remap = false)
public class RecipeOutputProviderMixin {

    // GTCEu 8.0 Jade API: write(CompoundTag, BlockAccessor, RecipeLogic)->void became write(RecipeLogic)->CompoundTag.
    // For a Stellar module return an empty tag so no recipe-output info is shown.
    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void cosmicCore$skipStellarModule(RecipeLogic capability, CallbackInfoReturnable<CompoundTag> cir) {
        if (capability.getMachine() instanceof StellarBaseModule) {
            cir.setReturnValue(new CompoundTag());
        }
    }
}
