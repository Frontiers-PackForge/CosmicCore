package com.ghostipedia.cosmiccore.common.compat.create;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;

public final class FactoryGaugePromiseLimitSupport {

    private static final String FLUID_PANEL = "net.liukrast.repackaged.content.fluid.FluidPanelBehaviour";

    private FactoryGaugePromiseLimitSupport() {}

    public static boolean supports(FactoryPanelBehaviour behaviour) {
        return behaviour.getClass() == FactoryPanelBehaviour.class || isFluid(behaviour);
    }

    public static boolean isFluid(FactoryPanelBehaviour behaviour) {
        return behaviour.getClass().getName().equals(FLUID_PANEL);
    }

    public static int normalize(FactoryPanelBehaviour behaviour, int limit) {
        if (limit < 0) return -1;
        int maximum = isFluid(behaviour) ? 16_000_000 : behaviour.panelBE().restocker ? 128_000 : 1_000;
        return Math.min(limit, maximum);
    }

    public static int effectiveRecipeLimit(FactoryPanelBehaviour behaviour, int limit) {
        if (limit < 0) return -1;
        long effective = (long) limit * Math.max(1, behaviour.recipeOutput);
        return effective > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) effective;
    }
}
