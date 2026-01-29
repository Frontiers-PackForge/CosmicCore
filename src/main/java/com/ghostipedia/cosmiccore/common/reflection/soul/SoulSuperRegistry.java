package com.ghostipedia.cosmiccore.common.reflection.soul;

import com.ghostipedia.cosmiccore.common.reflection.soul.impl.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for Soul Shape super abilities.
 */
public final class SoulSuperRegistry {

    private SoulSuperRegistry() {}

    private static final Map<SoulShape, SoulSuper> SUPERS = new EnumMap<>(SoulShape.class);

    static {
        register(new DefySuper());
        register(new DevourSuper());
        register(new OverclockSuper());
        register(new SlipstreamSuper());
        register(new LastStandSuper());
        register(new RipAndTearSuper());
    }

    private static void register(SoulSuper soulSuper) {
        SUPERS.put(soulSuper.getShape(), soulSuper);
    }

    /**
     * Get the super ability for a soul shape.
     */
    public static Optional<SoulSuper> get(SoulShape shape) {
        return Optional.ofNullable(SUPERS.get(shape));
    }
}
