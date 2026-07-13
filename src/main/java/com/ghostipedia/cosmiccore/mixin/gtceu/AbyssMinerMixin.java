package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.murkbloom.AbyssMachineRestrictions;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.MinerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.LargeMinerMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ MinerMachine.class, SteamMinerMachine.class, LargeMinerMachine.class })
public abstract class AbyssMinerMixin {

    @Inject(method = "drainInput", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableUndergardenMiner(boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        MetaMachine miner = (MetaMachine) (Object) this;
        if (AbyssMachineRestrictions.inUndergarden(miner.getLevel())) {
            cir.setReturnValue(false);
        }
    }
}
