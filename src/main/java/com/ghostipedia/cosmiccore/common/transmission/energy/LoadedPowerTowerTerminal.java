package com.ghostipedia.cosmiccore.common.transmission.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.utils.GTUtil;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record LoadedPowerTowerTerminal(UUID graphNodeId, int maximumVoltageTier,
                                       List<IEnergyContainer> inputHatches,
                                       List<IEnergyContainer> outputHatches) {

    public LoadedPowerTowerTerminal {
        Objects.requireNonNull(graphNodeId);
        Objects.requireNonNull(inputHatches);
        Objects.requireNonNull(outputHatches);
        if (maximumVoltageTier < 0 || maximumVoltageTier >= GTValues.V.length)
            throw new IllegalArgumentException("Invalid terminal voltage tier");
        inputHatches = List.copyOf(inputHatches);
        outputHatches = List.copyOf(outputHatches);
        if (!PowerTowerEnergyPolicy.acceptsHatchCount(inputHatches.size()) ||
                !PowerTowerEnergyPolicy.acceptsHatchCount(outputHatches.size()))
            throw new IllegalArgumentException("A terminal exceeds its energy hatch limit");
        if (inputHatches.isEmpty() && outputHatches.isEmpty())
            throw new IllegalArgumentException("A terminal requires at least one energy hatch");
        if (inputHatches.stream().anyMatch(container -> container == null || container.getInputAmperage() <= 0 ||
                exactTier(container.getInputVoltage()) < 0))
            throw new IllegalArgumentException("Invalid input hatch voltage");
        if (outputHatches.stream().anyMatch(container -> container == null || container.getOutputAmperage() <= 0 ||
                exactTier(container.getOutputVoltage()) < 0))
            throw new IllegalArgumentException("Invalid output hatch voltage");
        int actualMaximum = Math.max(
                inputHatches.stream().mapToInt(container -> exactTier(container.getInputVoltage())).max().orElse(-1),
                outputHatches.stream().mapToInt(container -> exactTier(container.getOutputVoltage())).max().orElse(-1));
        if (actualMaximum != maximumVoltageTier)
            throw new IllegalArgumentException("Terminal maximum voltage tier does not match its hatches");
    }

    public long inputCapacity() {
        return totalCapacity(inputHatches, true);
    }

    public long outputCapacity() {
        return totalCapacity(outputHatches, false);
    }

    private static int exactTier(long voltage) {
        int tier = GTUtil.getTierByVoltage(voltage);
        return tier >= 0 && tier < GTValues.V.length && GTValues.V[tier] == voltage ? tier : -1;
    }

    private static long totalCapacity(List<IEnergyContainer> hatches, boolean input) {
        long total = 0;
        for (IEnergyContainer hatch : hatches) {
            long voltage = input ? hatch.getInputVoltage() : hatch.getOutputVoltage();
            long amperage = input ? hatch.getInputAmperage() : hatch.getOutputAmperage();
            long capacity = saturatedMultiply(voltage, amperage);
            total = total > Long.MAX_VALUE - capacity ? Long.MAX_VALUE : total + capacity;
        }
        return total;
    }

    private static long saturatedMultiply(long first, long second) {
        if (first <= 0 || second <= 0) return 0;
        return first > Long.MAX_VALUE / second ? Long.MAX_VALUE : first * second;
    }
}
