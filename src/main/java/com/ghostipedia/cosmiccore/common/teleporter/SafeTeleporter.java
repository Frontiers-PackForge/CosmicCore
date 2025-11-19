package com.ghostipedia.cosmiccore.common.teleporter;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

// Saftey first kids (reusable ITeleporter that ensures safe arrival after dimension changes)
public class SafeTeleporter implements ITeleporter {

    private final BlockPos targetPos;
    private final boolean applySafetyBuffs;
    private final int buffDuration; // ticks

    // Create a SafeTeleporter with default settings (buffs enabled, 100 tick duration).
    public SafeTeleporter(BlockPos targetPos) {
        this(targetPos, true, 100);
    }

    // Create a SafeTeleporter with custom settings.
    public SafeTeleporter(BlockPos targetPos, boolean applySafetyBuffs, int buffDuration) {
        this.targetPos = targetPos;
        this.applySafetyBuffs = applySafetyBuffs;
        this.buffDuration = buffDuration;
    }

    @Override
    @Nullable
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
                                    Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        // Place entity at center of block, slightly above the pad
        Vec3 pos = new Vec3(
                targetPos.getX() + 0.5,
                targetPos.getY() + 0.1, // Slightly above to prevent clipping
                targetPos.getZ() + 0.5);

        // Zero velocity to prevent fall damage
        Vec3 velocity = Vec3.ZERO;

        // Preserve rotation
        return new PortalInfo(pos, velocity, entity.getYRot(), entity.getXRot());
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        Entity result = repositionEntity.apply(false);

        // Apply safety effects
        applySafetyEffects(result);

        return result;
    }

    private void applySafetyEffects(Entity entity) {
        // Clear fire
        entity.clearFire();

        // Reset fall distance to prevent fall damage
        entity.fallDistance = 0;

    }
}
