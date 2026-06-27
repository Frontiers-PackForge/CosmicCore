package com.ghostipedia.cosmiccore.integration.sable;

import net.minecraft.world.level.block.Rotation;

public final class SableAssemblyRotationHolder {

    private static final ThreadLocal<Rotation> CURRENT = new ThreadLocal<>();

    private SableAssemblyRotationHolder() {}

    public static void set(Rotation rotation) {
        CURRENT.set(rotation);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Rotation current() {
        Rotation rotation = CURRENT.get();
        return rotation == null ? Rotation.NONE : rotation;
    }
}
