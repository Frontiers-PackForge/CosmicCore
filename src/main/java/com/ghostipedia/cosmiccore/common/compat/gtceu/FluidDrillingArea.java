package com.ghostipedia.cosmiccore.common.compat.gtceu;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public interface FluidDrillingArea {

    boolean cosmiccore$isExpanded();

    void cosmiccore$setExpanded(boolean expanded);

    List<FluidStack> cosmiccore$getOutputs();
}
