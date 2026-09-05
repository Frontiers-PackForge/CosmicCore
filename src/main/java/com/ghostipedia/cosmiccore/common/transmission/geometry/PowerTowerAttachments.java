package com.ghostipedia.cosmiccore.common.transmission.geometry;

import com.ghostipedia.cosmiccore.common.data.CosmicMachines;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerRole;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PowerTowerAttachments {

    public static List<Vec3> resolve(BlockPattern pattern, BlockPos controller, Direction front, Direction up,
                                     boolean flipped) {
        var directions = pattern.getDirections();
        var sliceDirection = directions[0].getRelativeFacing(front, up, flipped);
        var rowDirection = directions[1].getRelativeFacing(front, up, flipped);
        var columnDirection = directions[2].getRelativeFacing(front, up, flipped);
        var origin = controller.mutable();
        pattern.getOffset().apply(origin, front, up, flipped);
        List<Vec3> points = new ArrayList<>();
        var slices = pattern.getSlices();
        for (int slice = 0; slice < slices.length; slice++) {
            char[][] rows = slices[slice].getPattern();
            for (int row = 0; row < rows.length; row++) {
                for (int column = 0; column < rows[row].length; column++) {
                    if (rows[row][column] != 'A') continue;
                    BlockPos position = origin.relative(sliceDirection, slice).relative(rowDirection, row)
                            .relative(columnDirection, column);
                    points.add(Vec3.atCenterOf(position).add(0, -0.5, 0));
                }
            }
        }
        return List.copyOf(points);
    }

    public static PowerTowerNode preview(BlockPos controller, Direction front, UUID owner) {
        var pattern = (BlockPattern) CosmicMachines.POWER_TOWER.getStructurePatterns()
                .get(MultiblockControllerMachine.DEFAULT_STRUCTURE).get();
        var points = resolve(pattern, controller, front, Direction.UP, false);
        var center = points.stream().reduce(Vec3.ZERO, Vec3::add).scale(1.0 / points.size());
        return new PowerTowerNode(new UUID(0, 0), controller, center, PowerTowerRole.DUMMY, owner, -1, true, points);
    }
}
