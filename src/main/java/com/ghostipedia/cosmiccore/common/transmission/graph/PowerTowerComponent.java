package com.ghostipedia.cosmiccore.common.transmission.graph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class PowerTowerComponent {

    private final UUID id;
    private final Map<UUID, PowerTowerNode> nodes = new LinkedHashMap<>();
    private final Map<UUID, PowerTowerSpan> spans = new LinkedHashMap<>();
    private long topologyVersion;

    PowerTowerComponent(UUID id) {
        this.id = id;
    }

    UUID id() {
        return id;
    }

    Map<UUID, PowerTowerNode> nodes() {
        return Collections.unmodifiableMap(nodes);
    }

    Map<UUID, PowerTowerSpan> spans() {
        return Collections.unmodifiableMap(spans);
    }

    long topologyVersion() {
        return topologyVersion;
    }

    void topologyVersion(long value) {
        topologyVersion = value;
    }

    void addNode(PowerTowerNode node) {
        nodes.put(node.id(), node);
    }

    void addSpan(PowerTowerSpan span) {
        spans.put(span.id(), span);
    }

    PowerTowerNode removeNode(UUID nodeId) {
        return nodes.remove(nodeId);
    }

    PowerTowerSpan removeSpan(UUID spanId) {
        return spans.remove(spanId);
    }
}
