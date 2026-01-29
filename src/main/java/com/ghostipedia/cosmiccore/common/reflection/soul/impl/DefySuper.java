package com.ghostipedia.cosmiccore.common.reflection.soul.impl;

import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulSuper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * REVENANT Super: Defy
 * When you would die, you don't. Enter a fury state with massive lifesteal.
 * Must heal to full HP or die for real when the window expires.
 */
public class DefySuper extends SoulSuper {

    private static final int COOLDOWN = 20 * 60 * 10; // 10 minutes
    private static final int DURATION = 20 * 20;      // 20 seconds
    private static final float LIFESTEAL = 0.5f;

    public DefySuper() {
        super(SoulShape.REVENANT, COOLDOWN);
    }

    @Override
    public int getDurationTicks() {
        return DURATION;
    }

    @Override
    public void activate(ServerPlayer player) {
        // Fury state: strength, speed, resistance
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION, 2, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION, 1, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION, 1, true, true, true));

        player.displayClientMessage(
                Component.literal("\u00A74\u00A7l*NOT YET.*"),
                true);
    }

    @Override
    public void tick(ServerPlayer player) {
        // Lifesteal is handled in damage event handler
    }

    @Override
    public void onEnd(ServerPlayer player) {
        // If not at full health when fury ends, die
        if (player.getHealth() < player.getMaxHealth()) {
            player.displayClientMessage(
                    Component.literal("\u00A74\u00A7o*The borrowed time runs out...*"),
                    true);
            player.hurt(player.damageSources().magic(), Float.MAX_VALUE);
        } else {
            player.displayClientMessage(
                    Component.literal("\u00A77\u00A7o*You have earned your second chance.*"),
                    true);
        }
    }

    public static float getLifesteal() {
        return LIFESTEAL;
    }
}
