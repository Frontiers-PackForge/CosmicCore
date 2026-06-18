package com.ghostipedia.cosmiccore.common.reflection;

import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;

import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Accessors for the Reflection system. Player reflection data lives in a NeoForge data
 * attachment that persists across death and dimension changes, so it is always present.
 */
public class ReflectionCapability {

    private ReflectionCapability() {}

    public static Optional<IReflection> get(Player player) {
        return Optional.of(player.getData(CosmicAttachmentTypes.REFLECTION));
    }

    public static IReflection getOrThrow(Player player) {
        return player.getData(CosmicAttachmentTypes.REFLECTION);
    }
}
