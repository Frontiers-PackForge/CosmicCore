package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.RainSealable;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = MetaMachine.class, remap = false)
public abstract class RainSealStateMixin implements RainSealable {

    @Unique
    @SaveField(nbtKey = "cosmiccore_rain_sealed")
    private boolean cosmiccore$rainSealed;

    @Override
    public boolean cosmiccore$isRainSealed() {
        return cosmiccore$rainSealed;
    }

    @Override
    public void cosmiccore$setRainSealed(boolean sealed) {
        cosmiccore$rainSealed = sealed;
        ((MetaMachine) (Object) this).setChanged();
    }
}
