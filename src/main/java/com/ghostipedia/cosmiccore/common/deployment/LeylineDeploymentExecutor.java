package com.ghostipedia.cosmiccore.common.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class LeylineDeploymentExecutor {

    private static final int TRANSACTIONAL_PLACEMENT_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE |
            Block.UPDATE_SUPPRESS_DROPS;

    private LeylineDeploymentExecutor() {}

    public static Result deployAtomically(ServerLevel level, LeylineDeploymentPlan plan, UUID ownerId,
                                          PlacementPermission placementPermission,
                                          PostPlacementValidator postPlacementValidator) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(plan);
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(placementPermission);
        Objects.requireNonNull(postPlacementValidator);
        LeylineDeploymentPreflight.Result preflight = LeylineDeploymentPreflight.validateFootprint(level, plan);
        if (!preflight.valid()) return new Result(Status.PREFLIGHT_FAILED, preflight.failures());
        for (var placement : plan.worldPlacements()) {
            BlockState current = level.getBlockState(placement.pos());
            if (!placementPermission.mayReplace(level, placement.pos(), current, placement.state())) {
                return new Result(Status.PERMISSION_DENIED, List.of());
            }
        }
        // Reject Block entities
        List<BlockStateSnapshot> snapshots = plan.worldPlacements().stream()
                .map(placement -> new BlockStateSnapshot(placement.pos(), level.getBlockState(placement.pos())))
                .toList();
        List<BlockPos> placedPositions = new ArrayList<>();
        try {
            for (var placement : plan.worldPlacements()) {
                level.setBlock(placement.pos(), placement.state(), TRANSACTIONAL_PLACEMENT_FLAGS);
                if (level.getBlockState(placement.pos()) != placement.state() &&
                        !level.getBlockState(placement.pos()).equals(placement.state())) {
                    restoreSnapshots(level, snapshots);
                    return new Result(Status.PLACEMENT_FAILED, List.of());
                }
                placedPositions.add(placement.pos());
            }
            for (var placement : plan.worldPlacements()) {
                if (!(placement.state().getBlock() instanceof MetaMachineBlock)) continue;
                MetaMachine machine = MetaMachine.getMachine(level, placement.pos());
                if (machine == null) {
                    restoreSnapshots(level, snapshots);
                    return new Result(Status.MACHINE_INITIALIZATION_FAILED, List.of());
                }
                machine.setOwnerUUID(ownerId);
            }
            if (!postPlacementValidator.validate(level, plan)) {
                restoreSnapshots(level, snapshots);
                return new Result(Status.POST_PLACEMENT_VALIDATION_FAILED, List.of());
            }
            for (BlockPos pos : placedPositions) level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
            return new Result(Status.COMMITTED, List.of());
        } catch (RuntimeException exception) {
            restoreSnapshots(level, snapshots);
            CosmicCore.LOGGER.error("Leyline deployment {} failed at {}", plan.blueprint().id(),
                    plan.controllerPos(), exception);
            return new Result(Status.PLACEMENT_FAILED, List.of());
        }
    }

    private static void restoreSnapshots(ServerLevel level, List<BlockStateSnapshot> snapshots) {
        for (int index = snapshots.size() - 1; index >= 0; index--) {
            BlockStateSnapshot snapshot = snapshots.get(index);
            level.setBlock(snapshot.pos(), snapshot.state(), TRANSACTIONAL_PLACEMENT_FLAGS);
        }
        for (BlockStateSnapshot snapshot : snapshots) {
            level.updateNeighborsAt(snapshot.pos(), level.getBlockState(snapshot.pos()).getBlock());
        }
    }

    public enum Status {
        COMMITTED,
        PREFLIGHT_FAILED,
        PERMISSION_DENIED,
        PLACEMENT_FAILED,
        MACHINE_INITIALIZATION_FAILED,
        POST_PLACEMENT_VALIDATION_FAILED
    }

    public record Result(Status status, List<LeylineDeploymentPreflight.Failure> failures) {

        public Result {
            Objects.requireNonNull(status);
            failures = List.copyOf(failures);
        }

        public boolean committed() {
            return status == Status.COMMITTED;
        }
    }

    @FunctionalInterface
    public interface PlacementPermission {

        boolean mayReplace(ServerLevel level, BlockPos pos, BlockState existing, BlockState placing);
    }

    @FunctionalInterface
    public interface PostPlacementValidator {

        boolean validate(ServerLevel level, LeylineDeploymentPlan plan);
    }

    private record BlockStateSnapshot(BlockPos pos, BlockState state) {}
}
