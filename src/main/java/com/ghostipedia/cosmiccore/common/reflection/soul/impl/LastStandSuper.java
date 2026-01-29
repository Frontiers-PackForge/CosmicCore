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
 * BULWARK Super: Last Stand
 * Plant yourself - you cannot move. Emit a damaging aura.
 * Take massively reduced damage. Enemies that hit you take reflect damage.
 * YOU are the hazard.
 */
public class LastStandSuper extends SoulSuper {

    private static final int COOLDOWN = 20 * 60 * 5;  // 5 minutes
    private static final int DURATION = 20 * 30;      // 30 seconds
    private static final double AURA_RADIUS = 4.0;
    private static final float AURA_DAMAGE = 2.0f;

    public LastStandSuper() {
        super(SoulShape.BULWARK, COOLDOWN);
    }

    @Override
    public int getDurationTicks() {
        return DURATION;
    }

    @Override
    public void activate(ServerPlayer player) {
        // Massive resistance, slowness to lock in place
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION, 3, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DURATION, 10, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0, true, true, true));

        player.displayClientMessage(
                Component.literal("\u00A78\u00A7l*I. WILL. NOT. MOVE.*"),
                true);
    }

    @Override
    public void tick(ServerPlayer player) {
        // Damage aura
        if (player.level().getGameTime() % 20 == 0) {
            AABB aura = player.getBoundingBox().inflate(AURA_RADIUS);
            List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    aura,
                    e -> e != player && e.isAlive());

            for (LivingEntity entity : nearby) {
                entity.hurt(player.damageSources().magic(), AURA_DAMAGE);
            }
        }
    }

    @Override
    public void onEnd(ServerPlayer player) {
        player.displayClientMessage(
                Component.literal("\u00A77\u00A7o*The ground remembers your stand.*"),
                true);
    }

    public static float getReflectDamageMultiplier() {
        return 0.5f;
    }
}
