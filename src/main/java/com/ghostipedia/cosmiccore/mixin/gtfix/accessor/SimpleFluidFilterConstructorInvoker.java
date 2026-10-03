package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;

import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = SimpleFluidFilter.class, remap = false)
public interface SimpleFluidFilterConstructorInvoker {

    @Invoker("<init>")
    static SimpleFluidFilter cosmiccore$create(boolean blacklist, boolean ignoreComponents, List<FluidStack> matches) {
        throw new AssertionError();
    }
}
