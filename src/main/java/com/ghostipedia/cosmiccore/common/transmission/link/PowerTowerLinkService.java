package com.ghostipedia.cosmiccore.common.transmission.link;

import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerWireGeometry;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerGraph;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public final class PowerTowerLinkService {

    public static final double DEFAULT_MAX_ATTACHMENT_DISTANCE = 96.0;
    public static final double MAX_DEPARTURE_ANGLE = 45.0;

    public static double departureAngle(PowerTowerNode node, PowerTowerNode other) {
        if (node.attachmentPoints().size() < 2) return 0;
        var arm = node.attachmentPoints().get(1).subtract(node.attachmentPoints().getFirst());
        var axis = arm.cross(new net.minecraft.world.phys.Vec3(0, 1, 0)).normalize();
        var span = other.wireAttachmentCenter().subtract(node.wireAttachmentCenter()).multiply(1, 0, 1).normalize();
        return Math.toDegrees(Math.acos(Math.clamp(Math.abs(axis.dot(span)), 0, 1)));
    }

    public static boolean isDepartureAngleAllowed(double angle) {
        return Double.isFinite(angle) && angle <= MAX_DEPARTURE_ANGLE + 1.0E-6;
    }

    public static double distance(PowerTowerNode first, PowerTowerNode second) {
        var points = PowerTowerWireGeometry.endpoints(first, second);
        double distance = 0;
        for (int i = 0; i < points.starts().size(); i++)
            distance = Math.max(distance, points.starts().get(i).distanceTo(points.ends().get(i)));
        return distance;
    }

    private final PowerTowerGraph graph;
    @Nullable
    private final PowerTowerSavedData savedData;
    private final double maxAttachmentDistance;

    public PowerTowerLinkService(PowerTowerGraph graph, @Nullable PowerTowerSavedData savedData) {
        this(graph, savedData, DEFAULT_MAX_ATTACHMENT_DISTANCE);
    }

    public PowerTowerLinkService(PowerTowerGraph graph, @Nullable PowerTowerSavedData savedData,
                                 double maxAttachmentDistance) {
        this.graph = Objects.requireNonNull(graph);
        this.savedData = savedData;
        if (!Double.isFinite(maxAttachmentDistance) || maxAttachmentDistance <= 0.0)
            throw new IllegalArgumentException("Maximum attachment distance must be positive");
        this.maxAttachmentDistance = maxAttachmentDistance;
    }

    public PowerTowerLinkResult tryCreateSpan(Level level, UUID firstNodeId, UUID secondNodeId, int cableVoltageTier,
                                              @Nullable Predicate<BlockPos> structureBlockExemption) {
        PowerTowerNode first = graph.node(firstNodeId);
        PowerTowerNode second = graph.node(secondNodeId);
        if (first == null || second == null)
            return new PowerTowerLinkResult(PowerTowerLinkResult.Status.ENDPOINT_NOT_FOUND, null);
        if (firstNodeId.equals(secondNodeId))
            return new PowerTowerLinkResult(PowerTowerLinkResult.Status.SELF_LINK, null);
        if (first.wireAttachmentCenter().distanceTo(second.wireAttachmentCenter()) > maxAttachmentDistance)
            return new PowerTowerLinkResult(PowerTowerLinkResult.Status.TOO_FAR, null);
        if (!Objects.equals(first.ownerId(), second.ownerId()))
            return new PowerTowerLinkResult(PowerTowerLinkResult.Status.OWNER_MISMATCH, null);
        Objects.requireNonNull(level);

        var failure = validatePath(level, first, second, structureBlockExemption);
        if (failure != null) return new PowerTowerLinkResult(failure, null);

        UUID spanId;
        try {
            spanId = graph.addSpan(firstNodeId, secondNodeId, cableVoltageTier);
        } catch (IllegalArgumentException exception) {
            return new PowerTowerLinkResult(PowerTowerLinkResult.Status.GRAPH_INVARIANT_REJECTED, null);
        }
        if (savedData != null) savedData.markGraphDirty();
        return new PowerTowerLinkResult(PowerTowerLinkResult.Status.CREATED, spanId);
    }

    public @Nullable PowerTowerLinkResult.Status validatePath(Level level, PowerTowerNode first, PowerTowerNode second,
                                                              @Nullable Predicate<BlockPos> structureBlockExemption) {
        if (!isDepartureAngleAllowed(Math.max(departureAngle(first, second), departureAngle(second, first))))
            return PowerTowerLinkResult.Status.BAD_ANGLE;

        PowerTowerWireGeometry wires;
        try {
            var endpoints = PowerTowerWireGeometry.endpoints(first, second);
            for (int i = 0; i < endpoints.starts().size(); i++) {
                if (endpoints.starts().get(i).distanceTo(endpoints.ends().get(i)) > maxAttachmentDistance)
                    return PowerTowerLinkResult.Status.TOO_FAR;
            }
            wires = endpoints.geometry();
        } catch (IllegalArgumentException exception) {
            return PowerTowerLinkResult.Status.GRAPH_INVARIANT_REJECTED;
        }

        Predicate<BlockPos> exempt = structureBlockExemption == null ? ignored -> false : structureBlockExemption;
        for (var segment : wires.segments()) {
            var bounds = segment.bounds();
            for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ),
                    BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ))) {
                if (!level.hasChunkAt(pos))
                    return PowerTowerLinkResult.Status.PATH_CHUNK_UNLOADED;
                if (occupiesCollisionBlock(level, pos, bounds, exempt))
                    return PowerTowerLinkResult.Status.PATH_OBSTRUCTED;
            }
        }

        return null;
    }

    private static boolean occupiesCollisionBlock(Level level, BlockPos pos, AABB bounds,
                                                  Predicate<BlockPos> exemption) {
        if (exemption.test(pos))
            return false;
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).toAabbs().stream()
                .anyMatch(box -> box.move(pos).intersects(bounds));
    }
}
