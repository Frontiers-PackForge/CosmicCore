package com.ghostipedia.cosmiccore.common.deployment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class LeylineDeploymentPreflight {

    private LeylineDeploymentPreflight() {}

    public static Result validateFootprint(Level level, LeylineDeploymentPlan plan) {
        if (level == null || plan == null) throw new NullPointerException();
        var failures = new ArrayList<Failure>();
        for (var placement : plan.worldPlacements()) {
            BlockPos pos = placement.pos();
            if (!withinWorld(level, pos)) {
                failures.add(new Failure(FailureKind.OUT_OF_BOUNDS, pos));
                continue;
            }
            if (!level.isLoaded(pos)) {
                failures.add(new Failure(FailureKind.CHUNK_NOT_LOADED, pos));
                continue;
            }
            if (!level.getFluidState(pos).isEmpty()) {
                failures.add(new Failure(FailureKind.FLUID_PRESENT, pos));
                continue;
            }
            if (level.getBlockEntity(pos) != null) {
                failures.add(new Failure(FailureKind.BLOCK_ENTITY_PRESENT, pos));
                continue;
            }
            if (!level.getBlockState(pos).canBeReplaced()) {
                failures.add(new Failure(FailureKind.NOT_REPLACEABLE, pos));
            }
        }
        return new Result(failures.isEmpty(), failures);
    }

    private static boolean withinWorld(Level level, BlockPos pos) {
        return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight() &&
                level.getWorldBorder().isWithinBounds(pos);
    }

    public enum FailureKind {
        OUT_OF_BOUNDS,
        CHUNK_NOT_LOADED,
        BLOCK_ENTITY_PRESENT,
        FLUID_PRESENT,
        NOT_REPLACEABLE
    }

    public record Failure(FailureKind kind, BlockPos pos) {

        public Failure {
            if (kind == null || pos == null) throw new NullPointerException();
            pos = pos.immutable();
        }
    }

    public record Result(boolean valid, List<Failure> failures) {

        public Result {
            if (failures == null) throw new NullPointerException();
            failures = List.copyOf(failures);
        }
    }
}
