package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;

@Mixin(value = RecipeLogicProvider.class, remap = false)
public class RecipeLogicProviderMixin {

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void cosmicCore$skipStellarModuleWrite(CompoundTag data, BlockAccessor blockAccessor,
                                                   RecipeLogic capability,
                                                   CallbackInfo ci) {
        if (capability.getMachine() instanceof StellarBaseModule) {
            data.putBoolean("Working", capability.isWorking());
            ci.cancel();
        }
    }
}
