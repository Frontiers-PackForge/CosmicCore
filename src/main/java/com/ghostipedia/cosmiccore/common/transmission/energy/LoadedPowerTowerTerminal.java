package com.ghostipedia.cosmiccore.common.transmission.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record LoadedPowerTowerTerminal(UUID graphNodeId, int voltageTier, List<IEnergyContainer> inputHatches,
                                       List<IEnergyContainer> outputHatches) {

    public LoadedPowerTowerTerminal {
        Objects.requireNonNull(graphNodeId);
        Objects.requireNonNull(inputHatches);
        Objects.requireNonNull(outputHatches);
        if (voltageTier < 0 || voltageTier >= GTValues.V.length)
            throw new IllegalArgumentException("Invalid terminal voltage tier");
        inputHatches = List.copyOf(inputHatches);
        outputHatches = List.copyOf(outputHatches);
        if (inputHatches.isEmpty() && outputHatches.isEmpty())
            throw new IllegalArgumentException("A terminal requires at least one energy hatch");
        long voltage = GTValues.V[voltageTier];
        if (inputHatches.stream().anyMatch(container -> container == null || container.getInputAmperage() <= 0 ||
                container.getInputVoltage() != voltage))
            throw new IllegalArgumentException("Input hatch voltage does not match the terminal");
        if (outputHatches.stream().anyMatch(container -> container == null || container.getOutputAmperage() <= 0 ||
                container.getOutputVoltage() != voltage))
            throw new IllegalArgumentException("Output hatch voltage does not match the terminal");
    }
}
