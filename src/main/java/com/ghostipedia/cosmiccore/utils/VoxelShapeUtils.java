package com.ghostipedia.cosmiccore.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class VoxelShapeUtils {

    private VoxelShapeUtils() {}

    public static VoxelShape rotateHorizontal(VoxelShape northShape, Direction facing) {
        if (facing == Direction.NORTH) return northShape;
        if (!facing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Expected a horizontal facing: " + facing);
        }
        VoxelShape result = Shapes.empty();
        for (var box : northShape.toAabbs()) {
            VoxelShape rotated = switch (facing) {
                case EAST -> Shapes.box(1 - box.maxZ, box.minY, box.minX,
                        1 - box.minZ, box.maxY, box.maxX);
                case SOUTH -> Shapes.box(1 - box.maxX, box.minY, 1 - box.maxZ,
                        1 - box.minX, box.maxY, 1 - box.minZ);
                case WEST -> Shapes.box(box.minZ, box.minY, 1 - box.maxX,
                        box.maxZ, box.maxY, 1 - box.minX);
                default -> throw new IllegalArgumentException("Expected a horizontal facing: " + facing);
            };
            result = Shapes.or(result, rotated);
        }
        return result;
    }
}
