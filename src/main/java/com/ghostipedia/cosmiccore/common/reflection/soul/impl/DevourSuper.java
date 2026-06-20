package com.ghostipedia.cosmiccore.common.reflection.soul.impl;

import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulSuper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * HOLLOW Super: Devour
 * Consume an entity whole. Gain permanent Nourishment buff and leech a stat.
 */
public class DevourSuper extends SoulSuper {

    private static final int COOLDOWN = 20 * 60 * 3; // 3 minutes
    private static final double DEVOUR_RANGE = 5.0;

    public DevourSuper() {
        super(SoulShape.HOLLOW, COOLDOWN);
    }

    @Override
    public void activate(ServerPlayer player) {
        // Find nearest non-player entity
        AABB range = player.getBoundingBox().inflate(DEVOUR_RANGE);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                range,
                e -> e != player && e.isAlive() && !e.isInvulnerable());

        if (targets.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("\u00A75\u00A7o*There is nothing to devour...*"),
                    true);
            return;
        }

        // Devour the nearest
        LivingEntity target = targets.get(0);
        double closestDist = player.distanceToSqr(target);
        for (LivingEntity e : targets) {
            double dist = player.distanceToSqr(e);
            if (dist < closestDist) {
                closestDist = dist;
                target = e;
            }
        }

        // Kill target instantly
        String victimName = target.getName().getString();
        target.kill();

        // Grant permanent saturation effect (Farmer's Delight Nourishment equivalent)
        // Since we may not have Farmer's Delight, use saturation + regeneration
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20 * 60, 0, true, false, true));

        // Grant a temporary stat boost based on target max health
        float targetHealth = target.getMaxHealth();
        if (targetHealth > 40) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 120, 1, true, true, true));
        } else if (targetHealth > 20) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, 0, true, true, true));
        }

        player.displayClientMessage(
                Component.literal("\u00A75\u00A7l*You consume " + victimName + " whole.*"),
                true);
    }
}
