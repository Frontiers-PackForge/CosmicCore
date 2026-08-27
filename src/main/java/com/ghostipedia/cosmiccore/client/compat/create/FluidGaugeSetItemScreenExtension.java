package com.ghostipedia.cosmiccore.client.compat.create;

import net.neoforged.neoforge.fluids.FluidStack;

public interface FluidGaugeSetItemScreenExtension {

    boolean cosmiccore$acceptFluidDrop(FluidStack fluid, int mouseX, int mouseY);
}
