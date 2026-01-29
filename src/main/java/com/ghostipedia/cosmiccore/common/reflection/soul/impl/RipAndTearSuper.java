package com.ghostipedia.cosmiccore.common.reflection.soul.impl;

import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulSuper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * BLOODTHIRST Super: Rip and Tear
 * Enter a frenzy state. Killing a mob lets you instantly dash to the next nearby mob.
 * Execute enemies below a health threshold.
 * Chains until nothing's left or you miss a kill.
 */
public class RipAndTearSuper extends SoulSuper {

    private static final int COOLDOWN = 20 * 60 * 5;  // 5 minutes
    private static final int DURATION = 20 * 30;      // 30 seconds
    private static final float EXECUTE_THRESHOLD = 0.3f; // 30% HP

    public RipAndTearSuper() {
        super(SoulShape.BLOODTHIRST, COOLDOWN);
    }

    @Override
    public int getDurationTicks() {
        return DURATION;
    }

    @Override
    public void activate(ServerPlayer player) {
        // Frenzy: strength, speed, no knockback taken
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION, 2, true, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION, 2, true, true, true));

        player.displayClientMessage(
                Component.literal("\u00A7c\u00A7l*RIP AND TEAR*"),
                true);
    }

    @Override
    public void onEnd(ServerPlayer player) {
        player.displayClientMessage(
                Component.literal("\u00A77\u00A7o*The frenzy subsides... for now.*"),
                true);
    }

    /**
     * @return health percentage threshold for execute
     */
    public static float getExecuteThreshold() {
        return EXECUTE_THRESHOLD;
    }
}
