package com.ghostipedia.cosmiccore.common.transmission.link;

import com.ghostipedia.cosmiccore.common.transmission.geometry.AttachmentBasis;
import com.ghostipedia.cosmiccore.common.transmission.geometry.TwinWireSagCurve;
import com.ghostipedia.cosmiccore.common.transmission.geometry.TwinWireSample;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerGraph;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public final class PowerTowerLinkService {

    public static final double DEFAULT_MAX_ATTACHMENT_DISTANCE = 64.0;
    public static final double MAX_SAMPLE_STEP = 0.25;

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
                                              double wireHalfSeparation, double sagDepth,
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

        TwinWireSagCurve wires;
        try {
            wires = TwinWireSagCurve.of(first.wireAttachmentCenter(), second.wireAttachmentCenter(),
                    AttachmentBasis.worldAligned(), wireHalfSeparation, sagDepth);
        } catch (IllegalArgumentException exception) {
            return new PowerTowerLinkResult(PowerTowerLinkResult.Status.GRAPH_INVARIANT_REJECTED, null);
        }

        for (var chunk : wires.coveredChunks()) {
            if (!level.hasChunkAt(new BlockPos(chunk.getMinBlockX(), 0, chunk.getMinBlockZ())))
                return new PowerTowerLinkResult(PowerTowerLinkResult.Status.PATH_CHUNK_UNLOADED, null);
        }

        Predicate<BlockPos> exempt = structureBlockExemption == null ? ignored -> false : structureBlockExemption;
        double spanDistance = first.wireAttachmentCenter().distanceTo(second.wireAttachmentCenter());
        int sampleSegments = Math.max(1,
                (int) Math.ceil((spanDistance + 8.0 * sagDepth) / MAX_SAMPLE_STEP));
        for (int i = 0; i <= sampleSegments; i++) {
            double t = (double) i / sampleSegments;
            TwinWireSample sample = wires.sample(t);
            if (occupiesCollisionBlock(level, sample.positiveLateral(), exempt) ||
                    occupiesCollisionBlock(level, sample.negativeLateral(), exempt))
                return new PowerTowerLinkResult(PowerTowerLinkResult.Status.PATH_OBSTRUCTED, null);
        }

        UUID spanId;
        try {
            // Construction validates once. added power towers never poll crossed chunks or collisions.
            spanId = graph.addSpan(firstNodeId, secondNodeId, cableVoltageTier);
        } catch (IllegalArgumentException exception) {
            return new PowerTowerLinkResult(PowerTowerLinkResult.Status.GRAPH_INVARIANT_REJECTED, null);
        }
        if (savedData != null)
            savedData.markGraphDirty();
        return new PowerTowerLinkResult(PowerTowerLinkResult.Status.CREATED, spanId);
    }

    private static boolean occupiesCollisionBlock(Level level, Vec3 point, Predicate<BlockPos> exemption) {
        BlockPos pos = BlockPos.containing(point);
        if (exemption.test(pos))
            return false;
        BlockState state = level.getBlockState(pos);
        return !state.getCollisionShape(level, pos).isEmpty();
    }
}
