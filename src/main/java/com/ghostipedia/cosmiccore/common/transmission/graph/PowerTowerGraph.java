package com.ghostipedia.cosmiccore.common.transmission.graph;

import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerWireGeometry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

public final class PowerTowerGraph {

    private final Map<UUID, PowerTowerComponent> components = new LinkedHashMap<>();
    private final Map<BlockPos, UUID> nodeByPosition = new HashMap<>();
    private final Map<UUID, UUID> componentByNode = new HashMap<>();
    private final Map<UUID, UUID> componentBySpan = new HashMap<>();
    private final Map<Long, Set<UUID>> nodesByChunk = new HashMap<>();
    private final Map<Long, Set<UUID>> spansByChunk = new HashMap<>();
    private final Map<UUID, Set<UUID>> adjacentNodes = new HashMap<>();
    private final Map<UUID, Set<UUID>> reachabilityCache = new HashMap<>();
    private final Map<UUID, PowerTowerWireGeometry> wireGeometry = new HashMap<>();

    public PowerTowerWireGeometry wireGeometry(UUID spanId) {
        return wireGeometry.get(spanId);
    }

    public boolean hasSpans() {
        return !componentBySpan.isEmpty();
    }

    public Map<UUID, ComponentSnapshot> componentSnapshots() {
        Map<UUID, ComponentSnapshot> result = new LinkedHashMap<>();
        components.forEach((id, component) -> result.put(id, snapshot(component)));
        return Collections.unmodifiableMap(result);
    }

    public PowerTowerNode nodeAtController(BlockPos controllerPos) {
        UUID id = nodeByPosition.get(controllerPos);
        UUID componentId = id == null ? null : componentByNode.get(id);
        PowerTowerComponent component = componentId == null ? null : components.get(componentId);
        return component == null ? null : component.nodes().get(id);
    }

    public PowerTowerNode node(UUID nodeId) {
        UUID componentId = componentByNode.get(nodeId);
        PowerTowerComponent component = componentId == null ? null : components.get(componentId);
        return component == null ? null : component.nodes().get(nodeId);
    }

    public PowerTowerSpan span(UUID spanId) {
        UUID componentId = componentBySpan.get(spanId);
        PowerTowerComponent component = componentId == null ? null : components.get(componentId);
        return component == null ? null : component.spans().get(spanId);
    }

    public ComponentSnapshot componentSnapshot(UUID componentId) {
        PowerTowerComponent component = components.get(componentId);
        return component == null ? null : snapshot(component);
    }

    public ComponentSnapshot componentAtController(BlockPos controllerPos) {
        PowerTowerNode node = nodeAtController(controllerPos);
        return node == null ? null : componentSnapshot(componentByNode.get(node.id()));
    }

    public ComponentSnapshot componentContainingNode(UUID nodeId) {
        UUID componentId = componentByNode.get(nodeId);
        return componentId == null ? null : componentSnapshot(componentId);
    }

    public Set<UUID> nodeIdsInChunk(long chunkKey) {
        return Collections.unmodifiableSet(new HashSet<>(nodesByChunk.getOrDefault(chunkKey, Set.of())));
    }

    public Set<UUID> spanIdsInChunk(long chunkKey) {
        return Collections.unmodifiableSet(new HashSet<>(spansByChunk.getOrDefault(chunkKey, Set.of())));
    }

    public UUID addNode(BlockPos controllerPos, Vec3 wireAttachmentCenter, PowerTowerRole role, UUID ownerId,
                        int terminalVoltageTier) {
        return addNode(UUID.randomUUID(), controllerPos, wireAttachmentCenter, role, ownerId, terminalVoltageTier);
    }

    public UUID addNode(BlockPos controllerPos, Vec3 wireAttachmentCenter, PowerTowerRole role, UUID ownerId,
                        int terminalVoltageTier, List<Vec3> attachmentPoints) {
        return addNode(UUID.randomUUID(), controllerPos, wireAttachmentCenter, role, ownerId, terminalVoltageTier,
                attachmentPoints);
    }

    public UUID addNode(UUID nodeId, BlockPos controllerPos, Vec3 wireAttachmentCenter, PowerTowerRole role,
                        UUID ownerId, int terminalVoltageTier) {
        return addNode(nodeId, controllerPos, wireAttachmentCenter, role, ownerId, terminalVoltageTier, List.of());
    }

    private UUID addNode(UUID nodeId, BlockPos controllerPos, Vec3 wireAttachmentCenter, PowerTowerRole role,
                         UUID ownerId, int terminalVoltageTier, List<Vec3> attachmentPoints) {
        if (nodeByPosition.containsKey(controllerPos) || componentByNode.containsKey(nodeId))
            throw new IllegalArgumentException("Power tower node already exists");
        UUID componentId = UUID.randomUUID();
        PowerTowerComponent component = new PowerTowerComponent(componentId);
        component.addNode(new PowerTowerNode(nodeId, controllerPos, wireAttachmentCenter, role, ownerId,
                terminalVoltageTier, true, attachmentPoints));
        components.put(componentId, component);
        indexNode(componentId, component.nodes().get(nodeId));
        recordTopologyMutation(component);
        return nodeId;
    }

    public boolean updateNode(UUID nodeId, Vec3 wireAttachmentCenter, PowerTowerRole role, UUID ownerId,
                              int terminalVoltageTier) {
        return updateNode(nodeId, wireAttachmentCenter, role, ownerId, terminalVoltageTier,
                node(nodeId).attachmentPoints());
    }

    public boolean updateNode(UUID nodeId, Vec3 wireAttachmentCenter, PowerTowerRole role, UUID ownerId,
                              int terminalVoltageTier, List<Vec3> attachmentPoints) {
        PowerTowerComponent component = requireComponentContainingNode(nodeId);
        PowerTowerNode current = component.nodes().get(nodeId);
        PowerTowerNode replacement = new PowerTowerNode(nodeId, current.controllerPos(), wireAttachmentCenter, role,
                ownerId, terminalVoltageTier, true, attachmentPoints);
        if (current.equals(replacement)) return false;
        for (PowerTowerNode other : component.nodes().values()) {
            if (!other.id().equals(nodeId) && !Objects.equals(other.ownerId(), ownerId))
                throw new IllegalArgumentException("Power tower owners must match");
        }
        component.spans().values()
                .forEach(span -> requireTerminalVoltageCompatibility(replacement, span.cableVoltageTier()));
        unindexNode(current);
        component.addNode(replacement);
        indexNode(component.id(), replacement);
        rebuildComponentSpanIndex(component);
        recordTopologyMutation(component);
        return true;
    }

    public boolean setStructureOperational(UUID nodeId, boolean structureOperational) {
        PowerTowerComponent component = requireComponentContainingNode(nodeId);
        PowerTowerNode current = component.nodes().get(nodeId);
        if (current.structureOperational() == structureOperational) return false;
        component.addNode(new PowerTowerNode(current.id(), current.controllerPos(), current.wireAttachmentCenter(),
                current.role(), current.ownerId(), current.terminalVoltageTier(), structureOperational,
                current.attachmentPoints()));
        recordTopologyMutation(component);
        return true;
    }

    public Set<UUID> reachableNodeIds(UUID sourceNode) {
        PowerTowerComponent component = requireComponentContainingNode(sourceNode);
        PowerTowerNode source = component.nodes().get(sourceNode);
        // Gate Functional behavior based on if we're formed
        if (!source.structureOperational()) {
            reachabilityCache.put(sourceNode, Set.of());
            return Set.of();
        }
        Set<UUID> cached = reachabilityCache.get(sourceNode);
        if (cached != null) return cached;
        Set<UUID> visited = new HashSet<>();
        ArrayDeque<UUID> queue = new ArrayDeque<>();
        visited.add(sourceNode);
        queue.add(sourceNode);
        while (!queue.isEmpty()) {
            UUID nodeId = queue.remove();
            for (UUID other : adjacentNodes.getOrDefault(nodeId, Set.of())) {
                PowerTowerNode otherNode = component.nodes().get(other);
                if (otherNode != null && otherNode.structureOperational() && visited.add(other)) {
                    queue.add(other);
                }
            }
        }
        Set<UUID> reachable = Collections.unmodifiableSet(visited);
        visited.forEach(nodeId -> reachabilityCache.put(nodeId, reachable));
        return reachable;
    }

    public UUID addSpan(UUID firstNodeId, UUID secondNodeId, int cableVoltageTier) {
        PowerTowerComponent first = requireComponentContainingNode(firstNodeId);
        PowerTowerComponent second = requireComponentContainingNode(secondNodeId);
        if (firstNodeId.equals(secondNodeId))
            throw new IllegalArgumentException("A span requires two distinct nodes");
        if (cableVoltageTier < 0)
            throw new IllegalArgumentException("Voltage tier cannot be negative");
        PowerTowerNode firstRecord = first.nodes().get(firstNodeId);
        PowerTowerNode secondRecord = second.nodes().get(secondNodeId);
        if (!firstRecord.structureOperational() || !secondRecord.structureOperational())
            throw new IllegalArgumentException("Power tower endpoints must be operational");
        if (!Objects.equals(firstRecord.ownerId(), secondRecord.ownerId()))
            throw new IllegalArgumentException("Power tower owners must match");
        requireTerminalVoltageCompatibility(firstRecord, cableVoltageTier);
        requireTerminalVoltageCompatibility(secondRecord, cableVoltageTier);
        boolean duplicate = first.spans().values().stream()
                .anyMatch(span -> connectsNodes(span, firstNodeId, secondNodeId));
        if (duplicate)
            throw new IllegalArgumentException("Power tower span already exists");
        requireComponentCableVoltage(first, cableVoltageTier);
        if (first != second)
            requireComponentCableVoltage(second, cableVoltageTier);
        PowerTowerComponent target = first.id().compareTo(second.id()) <= 0 ? first : second;
        if (first != second) {
            PowerTowerComponent source = target == first ? second : first;
            target.topologyVersion(Math.max(target.topologyVersion(), source.topologyVersion()));
            source.nodes().values().forEach(node -> {
                target.addNode(node);
                componentByNode.put(node.id(), target.id());
            });
            source.spans().values().forEach(span -> {
                target.addSpan(span);
                componentBySpan.put(span.id(), target.id());
            });
            components.remove(source.id());
            target.nodes().values().forEach(node -> indexNode(target.id(), node));
        }
        UUID spanId = UUID.randomUUID();
        PowerTowerSpan span = new PowerTowerSpan(spanId, firstNodeId, secondNodeId, cableVoltageTier);
        target.addSpan(span);
        componentBySpan.put(spanId, target.id());
        indexSpan(span);
        recordTopologyMutation(target);
        return spanId;
    }

    public boolean removeSpan(UUID spanId) {
        UUID componentId = componentBySpan.remove(spanId);
        if (componentId == null)
            return false;
        PowerTowerComponent component = components.get(componentId);
        PowerTowerSpan removed = component.removeSpan(spanId);
        unindexSpan(removed);
        recordTopologyMutation(component);
        splitDisconnectedComponent(component);
        return true;
    }

    public boolean removeNode(UUID nodeId) {
        UUID componentId = componentByNode.get(nodeId);
        PowerTowerComponent component = componentId == null ? null : components.get(componentId);
        if (component == null)
            return false;
        List<UUID> spans = component.spans().values().stream()
                .filter(span -> span.firstNodeId().equals(nodeId) || span.secondNodeId().equals(nodeId))
                .map(PowerTowerSpan::id).toList();
        for (UUID spanId : spans) {
            unindexSpan(component.removeSpan(spanId));
            componentBySpan.remove(spanId);
        }
        PowerTowerNode removed = component.removeNode(nodeId);
        unindexNode(removed);
        componentByNode.remove(nodeId);
        adjacentNodes.remove(nodeId);
        reachabilityCache.clear();
        if (component.nodes().isEmpty())
            components.remove(component.id());
        else {
            recordTopologyMutation(component);
            splitDisconnectedComponent(component);
        }
        return true;
    }

    public CompoundTag saveToTag() {
        CompoundTag root = new CompoundTag();
        ListTag componentList = new ListTag();
        components.values().stream().sorted(Comparator.comparing(PowerTowerComponent::id)).forEach(component -> {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Id", component.id());
            tag.putLong("Version", component.topologyVersion());
            ListTag nodes = new ListTag();
            component.nodes().values().stream().sorted(Comparator.comparing(PowerTowerNode::id)).forEach(node -> {
                CompoundTag nodeTag = new CompoundTag();
                nodeTag.putUUID("Id", node.id());
                nodeTag.put("Position", NbtUtils.writeBlockPos(node.controllerPos()));
                nodeTag.putDouble("AttachmentX", node.wireAttachmentCenter().x);
                nodeTag.putDouble("AttachmentY", node.wireAttachmentCenter().y);
                nodeTag.putDouble("AttachmentZ", node.wireAttachmentCenter().z);
                ListTag attachments = new ListTag();
                for (Vec3 point : node.attachmentPoints()) {
                    CompoundTag attachment = new CompoundTag();
                    attachment.putDouble("X", point.x);
                    attachment.putDouble("Y", point.y);
                    attachment.putDouble("Z", point.z);
                    attachments.add(attachment);
                }
                nodeTag.put("Attachments", attachments);
                nodeTag.putString("Role", node.role().name());
                if (node.ownerId() != null)
                    nodeTag.putUUID("Owner", node.ownerId());
                nodeTag.putInt("TerminalTier", node.terminalVoltageTier());
                nodeTag.putBoolean("Operational", node.structureOperational());
                nodes.add(nodeTag);
            });
            ListTag spans = new ListTag();
            component.spans().values().stream().sorted(Comparator.comparing(PowerTowerSpan::id)).forEach(span -> {
                CompoundTag spanTag = new CompoundTag();
                spanTag.putUUID("Id", span.id());
                spanTag.putUUID("First", span.firstNodeId());
                spanTag.putUUID("Second", span.secondNodeId());
                spanTag.putInt("Voltage", span.cableVoltageTier());
                spans.add(spanTag);
            });
            tag.put("Nodes", nodes);
            tag.put("Spans", spans);
            componentList.add(tag);
        });
        root.put("Components", componentList);
        return root;
    }

    public static PowerTowerGraph loadFromTag(CompoundTag root) {
        PowerTowerGraph graph = new PowerTowerGraph();
        ListTag list = root.getList("Components", Tag.TAG_COMPOUND);
        for (Tag raw : list) {
            CompoundTag tag = (CompoundTag) raw;
            UUID componentId = tag.getUUID("Id");
            PowerTowerComponent component = new PowerTowerComponent(componentId);
            component.topologyVersion(tag.getLong("Version"));
            for (Tag nodeRaw : tag.getList("Nodes", Tag.TAG_COMPOUND)) {
                CompoundTag nodeTag = (CompoundTag) nodeRaw;
                UUID owner = nodeTag.hasUUID("Owner") ? nodeTag.getUUID("Owner") : null;
                List<Vec3> attachments = new ArrayList<>();
                for (Tag entry : nodeTag.getList("Attachments", Tag.TAG_COMPOUND)) {
                    CompoundTag point = (CompoundTag) entry;
                    attachments.add(new Vec3(point.getDouble("X"), point.getDouble("Y"), point.getDouble("Z")));
                }
                PowerTowerNode node = new PowerTowerNode(nodeTag.getUUID("Id"),
                        NbtUtils.readBlockPos(nodeTag, "Position").orElseThrow(),
                        new Vec3(nodeTag.getDouble("AttachmentX"), nodeTag.getDouble("AttachmentY"),
                                nodeTag.getDouble("AttachmentZ")),
                        PowerTowerRole.valueOf(nodeTag.getString("Role")), owner, nodeTag.getInt("TerminalTier"),
                        !nodeTag.contains("Operational") || nodeTag.getBoolean("Operational"), attachments);
                component.addNode(node);
            }
            for (Tag spanRaw : tag.getList("Spans", Tag.TAG_COMPOUND)) {
                CompoundTag spanTag = (CompoundTag) spanRaw;
                component.addSpan(new PowerTowerSpan(spanTag.getUUID("Id"), spanTag.getUUID("First"),
                        spanTag.getUUID("Second"), spanTag.getInt("Voltage")));
            }
            graph.components.put(componentId, component);
        }
        graph.rebuildIndexes();
        return graph;
    }

    private void splitDisconnectedComponent(PowerTowerComponent component) {
        if (component.nodes().isEmpty()) {
            components.remove(component.id());
            return;
        }
        Set<UUID> remaining = new HashSet<>(component.nodes().keySet());
        List<Set<UUID>> partitions = new ArrayList<>();
        while (!remaining.isEmpty()) {
            UUID start = remaining.iterator().next();
            Set<UUID> partition = new HashSet<>();
            ArrayDeque<UUID> queue = new ArrayDeque<>();
            queue.add(start);
            remaining.remove(start);
            while (!queue.isEmpty()) {
                UUID nodeId = queue.remove();
                partition.add(nodeId);
                for (PowerTowerSpan span : component.spans().values()) {
                    UUID other = span.firstNodeId().equals(nodeId) ? span.secondNodeId() :
                            span.secondNodeId().equals(nodeId) ? span.firstNodeId() : null;
                    if (other != null && remaining.remove(other))
                        queue.add(other);
                }
            }
            partitions.add(partition);
        }
        if (partitions.size() <= 1)
            return;
        partitions.sort(Comparator.comparing(set -> set.stream().min(UUID::compareTo).orElseThrow()));
        List<PowerTowerSpan> oldSpans = new ArrayList<>(component.spans().values());
        components.remove(component.id());
        for (int i = 0; i < partitions.size(); i++) {
            UUID id = i == 0 ? component.id() : UUID.randomUUID();
            Set<UUID> partition = partitions.get(i);
            PowerTowerComponent replacement = new PowerTowerComponent(id);
            replacement.topologyVersion(component.topologyVersion() + 1);
            partition.forEach(nodeId -> replacement.addNode(component.nodes().get(nodeId)));
            oldSpans.stream()
                    .filter(span -> partition.contains(span.firstNodeId()) &&
                            partition.contains(span.secondNodeId()))
                    .forEach(replacement::addSpan);
            components.put(id, replacement);
        }
        rebuildIndexes();
    }

    private PowerTowerComponent requireComponentContainingNode(UUID nodeId) {
        UUID componentId = componentByNode.get(nodeId);
        PowerTowerComponent component = componentId == null ? null : components.get(componentId);
        if (component == null || !component.nodes().containsKey(nodeId))
            throw new IllegalArgumentException("Unknown power tower node");
        return component;
    }

    private void rebuildIndexes() {
        nodeByPosition.clear();
        componentByNode.clear();
        componentBySpan.clear();
        nodesByChunk.clear();
        spansByChunk.clear();
        adjacentNodes.clear();
        reachabilityCache.clear();
        components.values().forEach(component -> {
            component.nodes().values().forEach(node -> indexNode(component.id(), node));
            component.spans().values().forEach(span -> {
                componentBySpan.put(span.id(), component.id());
                indexSpan(span);
            });
        });
    }

    private void indexNode(UUID componentId, PowerTowerNode node) {
        nodeByPosition.put(node.controllerPos(), node.id());
        componentByNode.put(node.id(), componentId);
        nodesByChunk.computeIfAbsent(controllerChunkKey(node.controllerPos()), ignored -> new HashSet<>())
                .add(node.id());
        adjacentNodes.computeIfAbsent(node.id(), ignored -> new HashSet<>());
    }

    private void unindexNode(PowerTowerNode node) {
        nodeByPosition.remove(node.controllerPos());
        Set<UUID> ids = nodesByChunk.get(controllerChunkKey(node.controllerPos()));
        if (ids != null) {
            ids.remove(node.id());
            if (ids.isEmpty())
                nodesByChunk.remove(controllerChunkKey(node.controllerPos()));
        }
    }

    private static long controllerChunkKey(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private void indexSpan(PowerTowerSpan span) {
        PowerTowerNode first = node(span.firstNodeId());
        PowerTowerNode second = node(span.secondNodeId());
        if (first == null || second == null) return;
        adjacentNodes.computeIfAbsent(first.id(), ignored -> new HashSet<>()).add(second.id());
        adjacentNodes.computeIfAbsent(second.id(), ignored -> new HashSet<>()).add(first.id());
        var geometry = PowerTowerWireGeometry.endpoints(first, second).geometry();
        wireGeometry.put(span.id(), geometry);
        int minX = (int) Math.floor(geometry.bounds().minX);
        int maxX = (int) Math.floor(geometry.bounds().maxX);
        int minZ = (int) Math.floor(geometry.bounds().minZ);
        int maxZ = (int) Math.floor(geometry.bounds().maxZ);
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                spansByChunk.computeIfAbsent(ChunkPos.asLong(chunkX, chunkZ), ignored -> new HashSet<>())
                        .add(span.id());
            }
        }
    }

    private void unindexSpan(PowerTowerSpan span) {
        if (span == null) return;
        wireGeometry.remove(span.id());
        Set<UUID> first = adjacentNodes.get(span.firstNodeId());
        if (first != null) first.remove(span.secondNodeId());
        Set<UUID> second = adjacentNodes.get(span.secondNodeId());
        if (second != null) second.remove(span.firstNodeId());
        spansByChunk.values().removeIf(ids -> {
            ids.remove(span.id());
            return ids.isEmpty();
        });
    }

    private void rebuildComponentSpanIndex(PowerTowerComponent component) {
        component.spans().values().forEach(this::unindexSpan);
        component.spans().values().forEach(this::indexSpan);
    }

    private static boolean connectsNodes(PowerTowerSpan span, UUID first, UUID second) {
        return span.firstNodeId().equals(first) && span.secondNodeId().equals(second) ||
                span.firstNodeId().equals(second) && span.secondNodeId().equals(first);
    }

    private static void requireComponentCableVoltage(PowerTowerComponent component, int cableVoltageTier) {
        OptionalInt current = component.spans().values().stream().mapToInt(PowerTowerSpan::cableVoltageTier)
                .findFirst();
        if (current.isPresent() && current.getAsInt() != cableVoltageTier)
            throw new IllegalArgumentException("Power tower component voltage mismatch");
    }

    private static void requireTerminalVoltageCompatibility(PowerTowerNode node, int cableVoltageTier) {
        if (node.role() == PowerTowerRole.TERMINAL && node.terminalVoltageTier() != cableVoltageTier)
            throw new IllegalArgumentException("Power tower terminal voltage mismatch");
    }

    private void recordTopologyMutation(PowerTowerComponent component) {
        component.topologyVersion(component.topologyVersion() + 1);
        reachabilityCache.clear();
    }

    private static ComponentSnapshot snapshot(PowerTowerComponent component) {
        return new ComponentSnapshot(component.id(), component.topologyVersion(), component.nodes(),
                component.spans());
    }

    public record ComponentSnapshot(UUID id, long topologyVersion, Map<UUID, PowerTowerNode> nodes,
                                    Map<UUID, PowerTowerSpan> spans) {

        public ComponentSnapshot {
            nodes = Collections.unmodifiableMap(new LinkedHashMap<>(nodes));
            spans = Collections.unmodifiableMap(new LinkedHashMap<>(spans));
        }
    }
}
