package com.ghostipedia.cosmiccore.common.compat.create;

import net.neoforged.neoforge.fluids.FluidStack;

public interface FluidGaugeSetItemMenuExtension {

    boolean cosmiccore$isFluidGauge();

    FluidStack cosmiccore$getFluid();

    void cosmiccore$setFluid(FluidStack fluid);
}
