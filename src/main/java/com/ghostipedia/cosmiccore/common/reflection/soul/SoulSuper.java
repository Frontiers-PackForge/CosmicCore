package com.ghostipedia.cosmiccore.common.reflection.soul;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Base class for Soul Shape super abilities.
 * Each Soul Shape has one powerful active ability with a long cooldown.
 */
public abstract class SoulSuper {

    private final SoulShape shape;
    private final int cooldownTicks;

    protected SoulSuper(SoulShape shape, int cooldownTicks) {
        this.shape = shape;
        this.cooldownTicks = cooldownTicks;
    }

    /**
     * @return the soul shape this super belongs to
     */
    public SoulShape getShape() {
        return shape;
    }

    /**
     * @return cooldown in ticks
     */
    public int getCooldownTicks() {
        return cooldownTicks;
    }

    /**
     * @return cooldown in seconds for display
     */
    public int getCooldownSeconds() {
        return cooldownTicks / 20;
    }

    /**
     * @return translated name of this super ability
     */
    public Component getName() {
        return shape.getSuperName();
    }

    /**
     * @return translated description of this super ability
     */
    public Component getDescription() {
        return shape.getSuperDescription();
    }

    /**
     * Check if this super can be activated.
     * Override for custom conditions beyond cooldown.
     */
    public boolean canActivate(ServerPlayer player) {
        return true;
    }

    /**
     * Activate the super ability.
     * Called when player presses the super key and cooldown is ready.
     */
    public abstract void activate(ServerPlayer player);

    /**
     * Called every tick while the super effect is active.
     * Override for supers with duration-based effects.
     */
    public void tick(ServerPlayer player) {
        // Default: no tick behavior
    }

    /**
     * Called when the super effect ends.
     * Override for cleanup when duration expires.
     */
    public void onEnd(ServerPlayer player) {
        // Default: no cleanup
    }

    /**
     * @return duration of the super effect in ticks, or 0 if instant
     */
    public int getDurationTicks() {
        return 0;
    }
}
