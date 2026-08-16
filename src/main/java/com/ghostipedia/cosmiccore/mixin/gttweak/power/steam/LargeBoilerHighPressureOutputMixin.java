package com.ghostipedia.cosmiccore.mixin.gttweak.power.steam;

import com.ghostipedia.cosmiccore.common.power.steam.HPBoilerRates;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;

import net.neoforged.neoforge.fluids.FluidStack;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LargeBoilerMachine.class, remap = false)
public abstract class LargeBoilerHighPressureOutputMixin {

    @Shadow
    private int steamGenerated;

    @Redirect(
              method = "updateCurrentTemperature()V",
              at = @At(
                       value = "FIELD",
                       target = "Lcom/gregtechceu/gtceu/common/machine/multiblock/steam/LargeBoilerMachine;steamGenerated:I",
                       opcode = Opcodes.PUTFIELD),
              require = 2,
              expect = 2,
              allow = 2)
    private void cosmiccore$compressGeneratedSteam(LargeBoilerMachine machine, int amount) {
        steamGenerated = amount / HPBoilerRates.COMPACT_RATE;
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
        return HPBoilerRates.highPressureSteam(amount);
    }
}
