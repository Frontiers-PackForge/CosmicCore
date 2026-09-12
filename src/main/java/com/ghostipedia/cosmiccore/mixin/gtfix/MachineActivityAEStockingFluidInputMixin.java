package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine$ExportOnlyAEStockingFluidSlot",
       remap = false)
public abstract class MachineActivityAEStockingFluidInputMixin {

    @WrapMethod(method = "drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;")
    private FluidStack cosmiccore$input(int amount, FluidAction action, Operation<FluidStack> original) {
        return MachineActivityRuntime.recordedInput(original.call(amount, action), action);
    }
}
