package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

public final class VivariumYieldOverclock {

    private VivariumYieldOverclock() {}

    public static GTRecipe apply(GTRecipe original, long voltage) {
        GTRecipe result = original.copy();
        long eut = original.getInputEUt().voltage();
        if (eut <= 0) return result;
        int vitae = Math.max(0, original.data.getInt(BiomeldVivariumMachine.VITAE_BYPRODUCT));
        int spiritus = Math.max(0, original.data.getInt(BiomeldVivariumMachine.SPIRITUS_BYPRODUCT));
        int maximumCount = original.getOutputContents(ItemRecipeCapability.CAP).stream()
                .mapToInt(content -> ItemRecipeCapability.CAP.of(content.content()).count()).max().orElse(0);
        maximumCount = Math.max(maximumCount, Math.max(vitae, spiritus));
        if (maximumCount == 0) return result;
        long maximumYield = Integer.MAX_VALUE / maximumCount;
        int steps = 0;
        while (eut <= voltage / 4 && steps < 30 && (1L << (steps + 1)) <= maximumYield) {
            eut *= 4;
            steps++;
        }
        if (steps == 0) return result;
        int multiplier = 1 << steps;
        var yield = ContentModifier.multiplier(multiplier);
        if (!original.getOutputContents(ItemRecipeCapability.CAP).isEmpty()) {
            result.outputs.put(ItemRecipeCapability.CAP, original.getOutputContents(ItemRecipeCapability.CAP).stream()
                    .map(content -> content.copyChanced(ItemRecipeCapability.CAP, yield)).toList());
        }
        result.data = original.data.copy();
        if (vitae > 0) result.data.putInt(BiomeldVivariumMachine.VITAE_BYPRODUCT, vitae * multiplier);
        if (spiritus > 0) result.data.putInt(BiomeldVivariumMachine.SPIRITUS_BYPRODUCT, spiritus * multiplier);
        var energy = ContentModifier.multiplier(eut / original.getInputEUt().voltage());
        result.tickInputs.put(EURecipeCapability.CAP, original.tickInputs.get(EURecipeCapability.CAP).stream()
                .map(content -> content.copy(EURecipeCapability.CAP, energy)).toList());
        result.ocLevel = steps;
        return result;
    }
}
