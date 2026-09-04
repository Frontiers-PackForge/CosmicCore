package com.ghostipedia.cosmiccore.common.transmission.energy;

import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerGraph;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSpan;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PowerTowerEnergyRouter {

    private PowerTowerEnergyRouter() {}

    public static boolean hasTransferWork(PowerTowerGraph graph, LoadedPowerTowerTerminalRegistry loadedTerminals,
                                          UUID sourceNode) {
        LoadedPowerTowerTerminal source = loadedTerminals.terminal(sourceNode);
        PowerTowerGraph.ComponentSnapshot component = graph.componentContainingNode(sourceNode);
        if (source == null || component == null || component.spans().isEmpty() || !matchesVoltage(component, source))
            return false;
        Set<UUID> reachable = graph.reachableNodeIds(sourceNode);
        if (reachable.size() < 2) return false;
        boolean hasEnergy = source.inputHatches().stream().anyMatch(container -> container.getEnergyStored() > 0);
        if (!hasEnergy) return false;
        return loadedTerminals.reachableDestinations(component, sourceNode, reachable).stream()
                .filter(destination -> matchesVoltage(component, destination))
                .flatMap(destination -> destination.outputHatches().stream())
                .anyMatch(container -> container.getEnergyCanBeInserted() > 0);
    }

    public static TransferResult transferFrom(PowerTowerGraph graph,
                                              LoadedPowerTowerTerminalRegistry loadedTerminals,
                                              UUID sourceNode) {
        LoadedPowerTowerTerminal source = loadedTerminals.terminal(sourceNode);
        PowerTowerGraph.ComponentSnapshot component = graph.componentContainingNode(sourceNode);
        if (source == null || component == null || component.spans().isEmpty() || !matchesVoltage(component, source))
            return TransferResult.EMPTY;
        Set<UUID> reachable = graph.reachableNodeIds(sourceNode);
        List<LoadedPowerTowerTerminal> destinations = loadedTerminals.reachableDestinations(component, sourceNode,
                reachable);
        long transferred = 0;
        int reached = 0;
        // Matches vaguely to AT's behavioer. External hatch I/O remains the amp limit.
        for (LoadedPowerTowerTerminal destination : destinations) {
            if (!matchesVoltage(component, destination)) continue;
            long before = transferred;
            for (IEnergyContainer output : destination.outputHatches()) {
                for (IEnergyContainer input : source.inputHatches()) {
                    long requested = Math.min(input.getEnergyStored(), output.getEnergyCanBeInserted());
                    if (requested <= 0) continue;
                    long inserted = output.addEnergy(requested);
                    long removed = input.removeEnergy(inserted);
                    if (removed < inserted) output.removeEnergy(inserted - removed);
                    transferred = saturatedAdd(transferred, removed);
                }
            }
            if (transferred > before) reached++;
        }
        loadedTerminals.advanceDestinationCursor(sourceNode, destinations.size());
        return transferred == 0 ? TransferResult.EMPTY : new TransferResult(transferred, reached);
    }

    private static boolean matchesVoltage(PowerTowerGraph.ComponentSnapshot component,
                                          LoadedPowerTowerTerminal terminal) {
        PowerTowerSpan first = component.spans().values().stream().findFirst().orElse(null);
        return first != null && first.cableVoltageTier() == terminal.voltageTier();
    }

    private static long saturatedAdd(long first, long second) {
        if (second > 0 && first > Long.MAX_VALUE - second) return Long.MAX_VALUE;
        return first + second;
    }

    public record TransferResult(long transferredEu, int destinationsReached) {

        public static final TransferResult EMPTY = new TransferResult(0, 0);

        public TransferResult {
            if (transferredEu < 0 || destinationsReached < 0) throw new IllegalArgumentException();
        }
    }
}
