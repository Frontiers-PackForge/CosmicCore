package com.ghostipedia.cosmiccore.common.machine;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

public final class MachineOutputLimits {

    private MachineOutputLimits() {}

    public static int clampItemSlots(MetaMachine machine, int slots) {
        int limit = machine.getDefinition().getRecipeOutputLimits().getOrDefault(ItemRecipeCapability.CAP, -1);
        return limit > 0 ? Math.min(slots, limit) : slots;
    }
}
