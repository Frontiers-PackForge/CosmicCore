package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;

import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = NotifiableEnergyContainer.class, remap = false)
public abstract class MachineActivityEnergyMixin {

    @WrapMethod(method = "changeEnergy")
    private long cosmiccore$energy(long amount, Operation<Long> original) {
        long changed = original.call(amount);
        if (changed == Long.MIN_VALUE) ActivityScope.partial(true);
        else ActivityScope.value("energy", "gtceu:eu", Math.abs(changed), changed < 0);
        return changed;
    }
}
