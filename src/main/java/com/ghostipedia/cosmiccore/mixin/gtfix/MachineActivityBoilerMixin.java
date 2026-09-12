package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = LargeBoilerMachine.class, remap = false)
public abstract class MachineActivityBoilerMixin {

    @WrapMethod(method = "updateCurrentTemperature")
    private void cosmiccore$steam(Operation<Void> original) {
        try (ActivityScope ignored = MachineActivityRuntime.scope((MetaMachine) (Object) this)) {
            original.call();
        }
    }
}
