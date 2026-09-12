package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FluidTank.class, remap = false)
public abstract class MachineActivityFluidTankMixin {

    @WrapMethod(method = "fill")
    private int cosmiccore$fill(FluidStack stack, FluidAction action, Operation<Integer> original) {
        ActivityScope scope = ActivityScope.current();
        if (!ActivityScope.active() || action.simulate()) return original.call(stack, action);
        boolean root = scope.enterMutation();
        try {
            int amount = original.call(stack, action);
            if (root) ActivityScope.fluid(stack, amount, false);
            return amount;
        } catch (RuntimeException exception) {
            ActivityScope.partial(false);
            throw exception;
        } finally {
            scope.leaveMutation();
        }
    }

    @WrapMethod(method = "drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;")
    private FluidStack cosmiccore$drainAmount(int amount, FluidAction action, Operation<FluidStack> original) {
        ActivityScope scope = ActivityScope.current();
        if (!ActivityScope.active() || action.simulate()) return original.call(amount, action);
        boolean root = scope.enterMutation();
        try {
            FluidStack result = original.call(amount, action);
            if (root) ActivityScope.fluid(result, result.getAmount(), true);
            return result;
        } catch (RuntimeException exception) {
            ActivityScope.partial(true);
            throw exception;
        } finally {
            scope.leaveMutation();
        }
    }

    @WrapMethod(method = "drain(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;")
    private FluidStack cosmiccore$drainStack(FluidStack stack, FluidAction action, Operation<FluidStack> original) {
        ActivityScope scope = ActivityScope.current();
        if (!ActivityScope.active() || action.simulate()) return original.call(stack, action);
        boolean root = scope.enterMutation();
        try {
            FluidStack result = original.call(stack, action);
            if (root) ActivityScope.fluid(result, result.getAmount(), true);
            return result;
        } catch (RuntimeException exception) {
            ActivityScope.partial(true);
            throw exception;
        } finally {
            scope.leaveMutation();
        }
    }
}
