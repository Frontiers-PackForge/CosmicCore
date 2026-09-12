package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;

import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ExportOnlyAEFluidSlot.class, remap = false)
public abstract class MachineActivityAEFluidInputMixin {

    @WrapMethod(method = "drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;")
    private FluidStack cosmiccore$input(int amount, FluidAction action, Operation<FluidStack> original) {
        FluidStack extracted = original.call(amount, action);
        if (action.execute()) ActivityScope.fluid(extracted, extracted.getAmount(), true);
        return extracted;
    }
}
