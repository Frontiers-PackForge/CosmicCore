package com.ghostipedia.cosmiccore.common.teleporter;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/** Saftey first kids (builds a DimensionTransition that drops the entity safely on a target pad). */
public final class SafeTeleporter {

    private SafeTeleporter() {}

    public static DimensionTransition toSafe(ServerLevel destLevel, BlockPos targetPos, Entity entity) {
        Vec3 pos = new Vec3(
                targetPos.getX() + 0.5,
                targetPos.getY() + 0.1,
                targetPos.getZ() + 0.5);
        return new DimensionTransition(destLevel, pos, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                arrived -> {
                    arrived.clearFire();
                    arrived.fallDistance = 0;
                });
    }
}
