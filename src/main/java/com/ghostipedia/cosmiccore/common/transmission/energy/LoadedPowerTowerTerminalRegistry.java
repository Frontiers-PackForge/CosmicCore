package com.ghostipedia.cosmiccore.common.transmission.energy;

import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerGraph;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerRole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LoadedPowerTowerTerminalRegistry {

    private final Map<UUID, LoadedPowerTowerTerminal> loadedTerminals = new HashMap<>();
    private final Map<UUID, Runnable> transferWakeups = new HashMap<>();
    private final Map<UUID, Integer> destinationCursors = new HashMap<>();

    public void register(PowerTowerGraph graph, LoadedPowerTowerTerminal terminal) {
        register(graph, terminal, () -> {});
    }

    public void register(PowerTowerGraph graph, LoadedPowerTowerTerminal terminal, Runnable transferWakeup) {
        PowerTowerNode node = graph.node(terminal.graphNodeId());
        if (node == null || node.role() != PowerTowerRole.TERMINAL ||
                node.terminalVoltageTier() != terminal.voltageTier())
            throw new IllegalArgumentException("Loaded terminal does not match its persistent graph node");
        loadedTerminals.put(terminal.graphNodeId(), terminal);
        transferWakeups.put(terminal.graphNodeId(), transferWakeup);
        wakeComponentTerminals(graph, terminal.graphNodeId());
    }

    public void unregister(PowerTowerGraph graph, UUID nodeId, LoadedPowerTowerTerminal terminal) {
        if (loadedTerminals.get(nodeId) == terminal) {
            loadedTerminals.remove(nodeId);
            transferWakeups.remove(nodeId);
            destinationCursors.remove(nodeId);
            wakeComponentTerminals(graph, nodeId);
        }
    }

    public void wakeComponentTerminals(PowerTowerGraph graph, UUID nodeId) {
        PowerTowerGraph.ComponentSnapshot component = graph.componentContainingNode(nodeId);
        if (component == null) return;
        // Only loaded terminals wake up and transfer across the graph. relays do not flag chunks cause we'd explode TPS
        // lmoa
        component.nodes().keySet().stream().map(transferWakeups::get).filter(java.util.Objects::nonNull).toList()
                .forEach(Runnable::run);
    }

    public LoadedPowerTowerTerminal terminal(UUID nodeId) {
        return loadedTerminals.get(nodeId);
    }

    public List<LoadedPowerTowerTerminal> reachableDestinations(PowerTowerGraph.ComponentSnapshot component,
                                                                UUID sourceNode,
                                                                java.util.Set<UUID> reachableNodes) {
        List<LoadedPowerTowerTerminal> result = new ArrayList<>();
        for (PowerTowerNode node : component.nodes().values()) {
            if (node.id().equals(sourceNode) || node.role() != PowerTowerRole.TERMINAL ||
                    !reachableNodes.contains(node.id()))
                continue;
            LoadedPowerTowerTerminal terminal = loadedTerminals.get(node.id());
            if (terminal != null) result.add(terminal);
        }
        result.sort(Comparator.comparing(LoadedPowerTowerTerminal::graphNodeId));
        if (result.size() > 1) {
            int cursor = Math.floorMod(destinationCursors.getOrDefault(sourceNode, 0), result.size());
            java.util.Collections.rotate(result, -cursor);
        }
        return List.copyOf(result);
    }

    public void advanceDestinationCursor(UUID sourceNode, int destinationCount) {
        if (destinationCount <= 1) return;
        destinationCursors.compute(sourceNode,
                (ignored, cursor) -> Math.floorMod((cursor == null ? 0 : cursor) + 1, destinationCount));
    }
}
