package com.ghostipedia.cosmiccore.common.deployment;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record LeylineDeploymentBlueprint(ResourceLocation id, BlockPos controllerOffset,
                                         List<LeylineBlockPlacement> relativePlacements) {

    private static final Comparator<LeylineBlockPlacement> ORDER = Comparator
            .comparingInt((LeylineBlockPlacement placement) -> placement.relativeOffset().getX())
            .thenComparingInt(placement -> placement.relativeOffset().getY())
            .thenComparingInt(placement -> placement.relativeOffset().getZ());

    public LeylineDeploymentBlueprint {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(controllerOffset, "controllerOffset");
        Objects.requireNonNull(relativePlacements, "relativePlacements");
        BlockPos immutableControllerOffset = controllerOffset.immutable();
        var ordered = relativePlacements.stream().sorted(ORDER).toList();
        if (ordered.isEmpty()) throw new IllegalArgumentException("Blueprint has no placements");
        var uniqueOffsets = new HashSet<>(ordered.stream().map(LeylineBlockPlacement::relativeOffset).toList());
        if (uniqueOffsets.size() != ordered.size()) {
            throw new IllegalArgumentException("Blueprint contains duplicate placements");
        }
        if (ordered.stream().noneMatch(placement -> placement.relativeOffset().equals(immutableControllerOffset))) {
            throw new IllegalArgumentException("Blueprint is missing its controller anchor");
        }
        relativePlacements = List.copyOf(ordered);
        controllerOffset = immutableControllerOffset;
    }

    public static LeylineDeploymentBlueprint fromPopulated(ResourceLocation id, BlockPos controllerOffset,
                                                           Map<BlockPos, BlockState> populated) {
        Objects.requireNonNull(populated, "populated");
        var placements = new ArrayList<LeylineBlockPlacement>(populated.size());
        for (var entry : populated.entrySet()) {
            placements.add(new LeylineBlockPlacement(entry.getKey(), entry.getValue()));
        }
        return new LeylineDeploymentBlueprint(id, controllerOffset, placements);
    }

    public LeylineDeploymentPlan planAt(BlockPos controllerPos) {
        return new LeylineDeploymentPlan(this, controllerPos);
    }
}
