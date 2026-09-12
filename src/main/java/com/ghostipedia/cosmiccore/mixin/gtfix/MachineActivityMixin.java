package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.MachineActivity;
import com.ghostipedia.cosmiccore.api.machine.activity.MachineActivitySource;
import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MetaMachine.class, remap = false)
public abstract class MachineActivityMixin implements MachineActivitySource {

    @Unique
    @SaveField(nbtKey = "cosmiccore_activity")
    private final MachineActivity cosmiccore$activity = new MachineActivity();

    @Override
    public MachineActivity cosmiccore$activity() {
        return cosmiccore$activity;
    }

    @Inject(method = "onLoad", at = @At("HEAD"))
    private void cosmiccore$loaded(CallbackInfo ci) {
        MetaMachine machine = (MetaMachine) (Object) this;
        if (machine.getLevel() != null) cosmiccore$activity.loaded(machine.getLevel().registryAccess());
    }

    @Inject(method = "serverTick", at = @At("HEAD"))
    private void cosmiccore$tickActivity(CallbackInfo ci) {
        MachineActivityRuntime.tick((MetaMachine) (Object) this);
    }

    @Inject(method = "serverTick", at = @At("RETURN"))
    private void cosmiccore$activityStates(CallbackInfo ci) {
        MachineActivityRuntime.states((MetaMachine) (Object) this);
    }
}
