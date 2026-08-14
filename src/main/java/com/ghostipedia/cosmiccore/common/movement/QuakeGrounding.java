package com.ghostipedia.cosmiccore.common.movement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class QuakeGrounding {

    private static final double SUPPORT_DEPTH = 0.05;
    private static final double HORIZONTAL_INSET = 1.0E-4;

    private QuakeGrounding() {}

    public static boolean isMovementGrounded(Player player) {
        if (player.onGround()) return true;
        if (player.getDeltaMovement().y > 0.0) return false;

        AABB bounds = player.getBoundingBox();
        AABB supportProbe = new AABB(
                bounds.minX + HORIZONTAL_INSET,
                bounds.minY - SUPPORT_DEPTH,
                bounds.minZ + HORIZONTAL_INSET,
                bounds.maxX - HORIZONTAL_INSET,
                bounds.minY,
                bounds.maxZ - HORIZONTAL_INSET);
        return !player.level().noCollision(player, supportProbe);
    }
}
