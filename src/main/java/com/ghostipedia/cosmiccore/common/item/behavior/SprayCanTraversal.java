package com.ghostipedia.cosmiccore.common.item.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SprayCanTraversal {

    private SprayCanTraversal() {}

    public static List<BlockPos> line(BlockPos start, Direction direction, int limit) {
        int count = Math.max(1, limit);
        List<BlockPos> positions = new ArrayList<>(count);
        for (int offset = 0; offset < count; offset++) {
            positions.add(start.relative(direction, offset));
        }
        return positions;
    }

    public static List<BlockPos> between(BlockPos first, BlockPos second, int limit) {
        int changedAxes = (first.getX() == second.getX() ? 0 : 1) +
                (first.getY() == second.getY() ? 0 : 1) +
                (first.getZ() == second.getZ() ? 0 : 1);
        if (changedAxes > 1) return List.of();
        int distance = first.distManhattan(second);
        if (distance + 1 > Math.max(1, limit)) return List.of();
        if (distance == 0) return List.of(first);
        Direction direction = Direction.getNearest(second.getX() - first.getX(), second.getY() - first.getY(),
                second.getZ() - first.getZ());
        return line(first, direction, distance + 1);
    }

    public static boolean connectedColorMatches(ConnectionSignature seedType, int seedColor,
                                                ConnectionSignature candidateType, int candidateColor,
                                                boolean physicallyConnected) {
        return physicallyConnected && seedColor == candidateColor && Objects.equals(seedType, candidateType);
    }

    public static <T> List<T> validPrefix(List<T> candidates, Predicate<List<T>> validator) {
        for (int size = 1; size <= candidates.size(); size++) {
            if (!validator.test(candidates.subList(0, size))) return candidates.subList(0, size - 1);
        }
        return candidates;
    }

    public static <T> boolean isComplete(List<T> candidates, Predicate<List<T>> validator) {
        return !candidates.isEmpty() && validator.test(candidates);
    }

    public static <T> boolean allAuthorized(List<T> candidates, Predicate<T> authorization) {
        return candidates.stream().allMatch(authorization);
    }

    public static <T> boolean canEnter(T candidate, Predicate<T> authorization, boolean traversalMatches) {
        return traversalMatches && authorization.test(candidate);
    }

    public record ConnectionSignature(Object family, Object variant, Object material) {}
}
