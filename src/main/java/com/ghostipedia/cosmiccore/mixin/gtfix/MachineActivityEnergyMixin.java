package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.machine.trait.activity.EnergyMutationAdapter;

import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = NotifiableEnergyContainer.class, remap = false)
public abstract class MachineActivityEnergyMixin {

    @WrapMethod(method = "changeEnergy")
    private long cosmiccore$energy(long amount, Operation<Long> original) {
        return EnergyMutationAdapter.execute(amount, original::call);
    }
}
