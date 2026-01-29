package com.ghostipedia.cosmiccore.common.reflection.soul.impl;

import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulSuper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * ENGINE Super: Overclock
 * Enter hyperdrive state. Attack speed, move speed, mining speed all massively boosted.
 * Burns through resources/durability/hunger rapidly.
 */
public class OverclockSuper extends SoulSuper {

    private static final int COOLDOWN = 20 * 60 * 5;  // 5 minutes
    private static final int DURATION = 20 * 60 * 2;  // 2 minutes

    public OverclockSuper() {
        super(SoulShape.ENGINE, COOLDOWN);
    }

    @Override
    public int getDurationTicks() {
        return DURATION;
    }

    @Override
    public void activate(ServerPlayer player) {
        // Hyperdrive: speed, haste, jump boost
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION, 3, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, DURATION, 3, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, DURATION, 1, true, true, true));

        player.displayClientMessage(
                Component.literal("\u00A76\u00A7l*OVERCLOCK ENGAGED*"),
                true);
    }

    @Override
    public void tick(ServerPlayer player) {
        // Burn hunger rapidly
        if (player.level().getGameTime() % 10 == 0) {
            player.getFoodData().addExhaustion(1.0f);
        }
    }

    @Override
    public void onEnd(ServerPlayer player) {
        player.displayClientMessage(
                Component.literal("\u00A77\u00A7o*Systems returning to normal operating parameters.*"),
                true);
    }
}
