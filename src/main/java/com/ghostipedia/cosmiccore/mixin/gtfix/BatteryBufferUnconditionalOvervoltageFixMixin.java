package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine$EnergyBatteryTrait", remap = false)
public abstract class BatteryBufferUnconditionalOvervoltageFixMixin {

    @Inject(
            method = "acceptEnergyFromNetwork(Lnet/minecraft/core/Direction;JJ)J",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$failBeforeInventoryCheck(Direction side, long voltage, long amperage,
                                                     CallbackInfoReturnable<Long> cir) {
        IEnergyContainer container = (IEnergyContainer) (Object) this;
        if (voltage > container.getInputVoltage() && (side == null || container.inputsEnergy(side))) {
            MachineTrait trait = (MachineTrait) (Object) this;
            GTUtil.doExplosion(trait.getMachine().getLevel(), trait.getMachine().getBlockPos(),
                    GTUtil.getExplosionPower(voltage));
            cir.setReturnValue(Math.min(amperage, container.getInputAmperage()));
        }
    }
}
