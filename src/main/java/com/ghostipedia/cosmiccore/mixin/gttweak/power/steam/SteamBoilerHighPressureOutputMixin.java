package com.ghostipedia.cosmiccore.mixin.gttweak.power.steam;

import com.ghostipedia.cosmiccore.common.power.steam.HighPressureSteamRules;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamLiquidBoilerMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolarBoiler;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine;

import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SteamBoilerMachine.class, remap = false)
public abstract class SteamBoilerHighPressureOutputMixin {

    @Inject(
            method = "<init>(Lcom/gregtechceu/gtceu/api/blockentity/BlockEntityCreationInfo;Z)V",
            at = @At("RETURN"),
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$configureHighPressureSteamTank(BlockEntityCreationInfo info, boolean isHighPressure,
                                                           CallbackInfo ci) {
        SteamBoilerMachine machine = (SteamBoilerMachine) (Object) this;
        if (isHighPressure) {
            machine.steamTank.setFilter(HighPressureSteamRules::isHighPressureSteam);
        }
    }

    @Inject(
            method = "getTotalSteamOutput()J",
            at = @At("RETURN"),
            cancellable = true,
            require = 2,
            expect = 2,
            allow = 2)
    private void cosmiccore$scaleHighPressureSteamOutput(CallbackInfoReturnable<Long> cir) {
        SteamBoilerMachine machine = (SteamBoilerMachine) (Object) this;
        if (!machine.isHighPressure() || cir.getReturnValue() == 0) return;

        int maximumCycleOutput;
        if (machine instanceof SteamSolarBoiler || machine instanceof SteamSolidBoilerMachine) {
            maximumCycleOutput = 20;
        } else if (machine instanceof SteamLiquidBoilerMachine) {
            maximumCycleOutput = 40;
        } else {
            return;
        }
        cir.setReturnValue((long) maximumCycleOutput * machine.getCurrentTemperature() / machine.getMaxTemperature());
    }

    @Redirect(
              method = "updateCurrentTemperature()V",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;getFluid(I)Lnet/neoforged/neoforge/fluids/FluidStack;"),
              require = 1,
              expect = 1,
              allow = 1)
    private FluidStack cosmiccore$emitHighPressureSteam(Material material, int amount) {
        SteamBoilerMachine machine = (SteamBoilerMachine) (Object) this;
        return machine.isHighPressure() ? HighPressureSteamRules.highPressureSteam(amount) : material.getFluid(amount);
    }
}
