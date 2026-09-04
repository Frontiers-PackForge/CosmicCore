package com.ghostipedia.cosmiccore.common.deployment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;

public record LeylineDeploymentPlan(LeylineDeploymentBlueprint blueprint, BlockPos controllerPos,
                                    List<WorldPlacement> worldPlacements) {

    public LeylineDeploymentPlan(LeylineDeploymentBlueprint blueprint, BlockPos controllerPos) {
        this(blueprint, controllerPos, blueprint.relativePlacements().stream()
                .map(placement -> new WorldPlacement(
                        controllerPos.offset(placement.relativeOffset().subtract(blueprint.controllerOffset())),
                        placement.state()))
                .toList());
    }

    public LeylineDeploymentPlan {
        if (blueprint == null || controllerPos == null || worldPlacements == null) {
            throw new NullPointerException();
        }
        controllerPos = controllerPos.immutable();
        worldPlacements = List.copyOf(worldPlacements);
        if (new HashSet<>(worldPlacements.stream().map(WorldPlacement::pos).toList()).size() !=
                worldPlacements.size()) {
            throw new IllegalArgumentException("Plan contains duplicate targets");
        }
    }

    public record WorldPlacement(BlockPos pos, BlockState state) {

        public WorldPlacement {
            if (pos == null || state == null) throw new NullPointerException();
            pos = pos.immutable();
        }
    }
}
