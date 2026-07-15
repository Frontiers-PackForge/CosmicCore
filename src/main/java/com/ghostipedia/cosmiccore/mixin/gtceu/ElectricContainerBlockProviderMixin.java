package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.integration.jade.provider.ElectricContainerBlockProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ElectricContainerBlockProvider.class, remap = false)
public class ElectricContainerBlockProviderMixin {

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void cosmicCore$skipStellarModule(CompoundTag data, IEnergyInfoProvider capability, CallbackInfo ci) {
        if (capability instanceof NotifiableEnergyContainer container) {
            if (container.getMachine() instanceof StellarBaseModule) {
                ci.cancel();
            }
        }
    }
}
