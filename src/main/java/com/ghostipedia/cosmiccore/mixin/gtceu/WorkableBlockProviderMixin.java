package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.integration.jade.provider.WorkableBlockProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorkableBlockProvider.class, remap = false)
public class WorkableBlockProviderMixin {

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void cosmicCore$skipStellarModule(CompoundTag data, IWorkable capability, CallbackInfo ci) {
        if (capability instanceof StellarBaseModule) {
            ci.cancel();
        }
    }
}
