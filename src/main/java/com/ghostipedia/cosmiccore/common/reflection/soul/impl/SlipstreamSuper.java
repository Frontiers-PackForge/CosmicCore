package com.ghostipedia.cosmiccore.common.reflection.soul.impl;

import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulSuper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * GLOBEDANCER Super: Slipstream
 * Become untouchable. No fall damage, no collision damage, phase through entities.
 * Step height maxed, speed boosted. Pure fluid motion.
 */
public class SlipstreamSuper extends SoulSuper {

    private static final int COOLDOWN = 20 * 60 * 5;  // 5 minutes
    private static final int DURATION = 20 * 60;      // 1 minute

    public SlipstreamSuper() {
        super(SoulShape.GLOBEDANCER, COOLDOWN);
    }

    @Override
    public int getDurationTicks() {
        return DURATION;
    }

    @Override
    public void activate(ServerPlayer player) {
        // Speed + jump boost (fall damage handled in tick)
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION, 2, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, DURATION, 2, true, true, true));

        // Mark player as in slipstream for damage immunity
        // This is checked in damage event handler

        player.displayClientMessage(
                Component.literal("\u00A7b\u00A7l*You slip between moments.*"),
                true);
    }

    @Override
    public void tick(ServerPlayer player) {
        // Nullify fall distance continuously
        player.fallDistance = 0;
    }

    @Override
    public void onEnd(ServerPlayer player) {
        player.displayClientMessage(
                Component.literal("\u00A77\u00A7o*The world catches up.*"),
                true);
    }
}
