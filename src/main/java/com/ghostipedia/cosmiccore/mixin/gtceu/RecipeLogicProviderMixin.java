package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeLogicProvider.class, remap = false)
public class RecipeLogicProviderMixin {

    // GTCEu 8.0 Jade API: write(CompoundTag, BlockAccessor, RecipeLogic)->void became write(RecipeLogic)->CompoundTag.
    // For a Stellar module return a tag carrying ONLY the working flag, suppressing the rest of the recipe info.
    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void cosmicCore$skipStellarModuleWrite(RecipeLogic capability, CallbackInfoReturnable<CompoundTag> cir) {
        if (capability.getMachine() instanceof StellarBaseModule) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Working", capability.isWorking());
            cir.setReturnValue(tag);
        }
    }
}
