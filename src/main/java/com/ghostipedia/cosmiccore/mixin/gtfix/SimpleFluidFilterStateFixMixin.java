package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;

import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(value = SimpleFluidFilter.class, remap = false)
public abstract class SimpleFluidFilterStateFixMixin {

    @Unique
    private static final int cosmiccore$MAX_FLUID_AMOUNT = 2_048_000_000;

    @Shadow
    protected int maxStackSize;

    @Shadow
    protected FluidStack[] matches;

    @Shadow
    @Final
    private CustomFluidTank[] fluidStorageSlots;

    @ModifyConstant(method = "<init>()V", constant = @Constant(intValue = 64000))
    private int cosmiccore$allowConfiguredAmounts(int original) {
        return cosmiccore$MAX_FLUID_AMOUNT;
    }

    @Inject(method = { "<init>()V", "<init>(ZZLjava/util/List;)V" }, at = @At("RETURN"), require = 2)
    private void cosmiccore$initializeAmountLimit(CallbackInfo ci) {
        maxStackSize = cosmiccore$MAX_FLUID_AMOUNT;
    }

    @Inject(method = "<init>(ZZLjava/util/List;)V", at = @At("TAIL"))
    private void cosmiccore$restoreFluidSlots(boolean isBlacklist, boolean ignoreComponents,
                                              List<FluidStack> decodedMatches, CallbackInfo ci) {
        matches = new FluidStack[fluidStorageSlots.length];
        Arrays.fill(matches, FluidStack.EMPTY);
        for (int i = 0; i < Math.min(decodedMatches.size(), matches.length); i++) {
            matches[i] = decodedMatches.get(i).copy();
        }
        for (int i = 0; i < fluidStorageSlots.length; i++) {
            int slot = i;
            fluidStorageSlots[i] = new CustomFluidTank(cosmiccore$MAX_FLUID_AMOUNT);
            fluidStorageSlots[i].setFluid(matches[i].copy());
            fluidStorageSlots[i].setOnContentsChanged(() -> {
                matches[slot] = fluidStorageSlots[slot].getFluid();
                ((SimpleFluidFilter) (Object) this).updateAndSaveFilter();
            });
        }
    }
}
