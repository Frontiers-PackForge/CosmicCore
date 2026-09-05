package com.ghostipedia.cosmiccore.common.deployment;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record LeylineDeploymentAnimation(AABB bounds, double openingY) {

    public static final int START_DELAY_TICKS = 2;
    public static final int OPEN_TICKS = 16;
    public static final int DESCENT_TICKS = 92;
    public static final int IMPACT_TICK = OPEN_TICKS + DESCENT_TICKS;
    public static final int CLOSE_TICKS = 12;
    public static final int CLIENT_TIMEOUT_TICKS = IMPACT_TICK + 200;
    public static final double PREFERRED_CLEARANCE = 15;
    public static final double CIRCLE_MARGIN = 0.25;

    public static LeylineDeploymentAnimation resolve(Level level, LeylineDeploymentPlan plan) {
        AABB bounds = bounds(plan);
        double radius = Math.hypot(bounds.getXsize(), bounds.getZsize()) / 2 + CIRCLE_MARGIN;
        double centerX = bounds.getCenter().x;
        double centerZ = bounds.getCenter().z;
        double openingY = Math.min(bounds.maxY + PREFERRED_CLEARANCE, level.getMaxBuildHeight());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(centerX - radius); x < Mth.ceil(centerX + radius); x++) {
            for (int z = Mth.floor(centerZ - radius); z < Mth.ceil(centerZ + radius); z++) {
                double dx = Math.max(Math.abs(x + 0.5 - centerX) - 0.5, 0);
                double dz = Math.max(Math.abs(z + 0.5 - centerZ) - 0.5, 0);
                if (dx * dx + dz * dz > radius * radius) continue;
                for (int y = Mth.floor(bounds.maxY); y < Mth.ceil(openingY); y++) {
                    cursor.set(x, y, z);
                    if (!level.isLoaded(cursor) || !level.getBlockState(cursor).isAir()) {
                        openingY = y;
                        break;
                    }
                }
            }
        }
        return new LeylineDeploymentAnimation(bounds, Math.max(bounds.maxY, openingY));
    }

    public static AABB bounds(LeylineDeploymentPlan plan) {
        AABB bounds = null;
        for (var placement : plan.worldPlacements()) {
            if (placement.state().isAir()) continue;
            AABB block = new AABB(placement.pos());
            bounds = bounds == null ? block : bounds.minmax(block);
        }
        if (bounds == null) throw new IllegalArgumentException("Deployment has no visible blocks");
        return bounds;
    }

    public double radius() {
        return Math.hypot(bounds.getXsize(), bounds.getZsize()) / 2 + CIRCLE_MARGIN;
    }

    public double verticalOffset(double age) {
        double progress = Mth.clamp((age - OPEN_TICKS) / DESCENT_TICKS, 0, 1);
        double descent = Math.pow(progress, 4);
        return (openingY - bounds.minY) * (1 - descent);
    }

    public float circleScale(double age, double impactAge) {
        double opening = Mth.clamp(age / OPEN_TICKS, 0, 1);
        double closing = Mth.clamp((age - impactAge) / CLOSE_TICKS, 0, 1);
        return (float) ((1 - Math.pow(1 - opening, 3)) * (1 - closing * closing));
    }
}
