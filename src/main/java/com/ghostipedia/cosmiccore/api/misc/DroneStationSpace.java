package com.ghostipedia.cosmiccore.api.misc;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import dev.ryanhcode.sable.Sable;
import org.jetbrains.annotations.Nullable;

public final class DroneStationSpace {

    private DroneStationSpace() {}

    public static double squaredDistance(@Nullable Level level, BlockPos first, BlockPos second) {
        if (level != null && GTCEu.isModLoaded("sable")) return SableIntegration.squaredDistance(level, first, second);
        return rawSquaredDistance(first, second);
    }

    public static BlockPos worldPosition(@Nullable Level level, BlockPos position) {
        if (level != null && GTCEu.isModLoaded("sable")) return SableIntegration.worldPosition(level, position);
        return position;
    }

    static long rawSquaredDistance(BlockPos first, BlockPos second) {
        long x = (long) first.getX() - second.getX();
        long y = (long) first.getY() - second.getY();
        long z = (long) first.getZ() - second.getZ();
        return x * x + y * y + z * z;
    }

    private static final class SableIntegration {

        private static double squaredDistance(Level level, BlockPos first, BlockPos second) {
            return Sable.HELPER.distanceSquaredWithSubLevels(level, Vec3.atCenterOf(first), Vec3.atCenterOf(second));
        }

        private static BlockPos worldPosition(Level level, BlockPos position) {
            Vec3 projected = Sable.HELPER.projectOutOfSubLevel(level, Vec3.atCenterOf(position));
            return BlockPos.containing(projected);
        }
    }
}
