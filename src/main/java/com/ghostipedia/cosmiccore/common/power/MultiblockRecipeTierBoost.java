package com.ghostipedia.cosmiccore.common.power;

import com.ghostipedia.cosmiccore.api.machine.multiblock.RecipeTierBoostState;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.utils.GTUtil;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class MultiblockRecipeTierBoost {

    public static final long REQUIRED_AMPS = 4L;

    private MultiblockRecipeTierBoost() {}

    public static boolean supportsRecipeTierBoost(MultiblockControllerMachine machine) {
        return machine.getStructurePatterns().values().stream()
                .filter(BlockPattern.class::isInstance)
                .map(BlockPattern.class::cast)
                .anyMatch(MultiblockRecipeTierBoost::allowsMultipleEnergyInputs);
    }

    public static RecipeTierBoostState evaluate(WorkableElectricMultiblockMachine machine,
                                                boolean supportsRecipeTierBoost) {
        long highestInputVoltage = 0L;
        long highestInputAmperage = 0L;
        int highestInputContainerCount = 0;
        long maximumThroughput = 0L;

        for (IRecipeHandler<?> handler : machine.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP)) {
            if (!(handler instanceof IEnergyContainer container)) continue;
            long inputVoltage = container.getInputVoltage();
            long inputAmperage = container.getInputAmperage();
            maximumThroughput = saturatedAdd(maximumThroughput, saturatedMultiply(inputVoltage, inputAmperage));
            if (inputVoltage > highestInputVoltage) {
                highestInputVoltage = inputVoltage;
                highestInputAmperage = inputAmperage;
                highestInputContainerCount = 1;
            } else if (inputVoltage == highestInputVoltage && inputVoltage > 0L) {
                highestInputAmperage = saturatedAdd(highestInputAmperage, inputAmperage);
                highestInputContainerCount++;
            }
        }

        int inputTier = GTUtil.getFloorTierByVoltage(highestInputVoltage);
        boolean boostApplied = supportsRecipeTierBoost && highestInputAmperage >= REQUIRED_AMPS &&
                inputTier < GTValues.MAX;
        return new RecipeTierBoostState(highestInputVoltage, highestInputAmperage,
                highestInputContainerCount, maximumThroughput, boostApplied);
    }

    private static boolean allowsMultipleEnergyInputs(BlockPattern pattern) {
        Set<BasePredicate> energyPredicates = Collections.newSetFromMap(new IdentityHashMap<>());
        for (MultiPredicate predicate : pattern.getPredicates().values()) {
            for (BasePredicate basePredicate : predicate.expand()) {
                if (acceptsEnergyHatches(basePredicate)) energyPredicates.add(basePredicate);
            }
        }

        int maximumInputs = 0;
        for (BasePredicate predicate : energyPredicates) {
            if (predicate.getMaxCount() < 0) return true;
            maximumInputs += predicate.getMaxCount();
            if (maximumInputs > 1) return true;
        }
        return false;
    }

    private static boolean acceptsEnergyHatches(BasePredicate predicate) {
        return predicate.getCandidates().stream()
                .anyMatch(info -> PartAbility.INPUT_ENERGY.isApplicable(info.getBlockState().getBlock()));
    }

    private static long saturatedMultiply(long left, long right) {
        if (left == 0L || right == 0L) return 0L;
        if (left > Long.MAX_VALUE / right) return Long.MAX_VALUE;
        return left * right;
    }

    private static long saturatedAdd(long left, long right) {
        if (Long.MAX_VALUE - left < right) return Long.MAX_VALUE;
        return left + right;
    }
}
