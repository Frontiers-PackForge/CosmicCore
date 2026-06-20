package com.ghostipedia.cosmiccore.api.block;

import com.ghostipedia.cosmiccore.api.CosmicCoreAPI;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public interface IMagnetType {

    /**
     * @return The Unique Name of the Magnet
     */
    @NotNull
    String getName();

    /**
     *
     * @return Maximum capacity of the magnet
     */
    int getMagnetFieldCapacity();

    /**
     *
     * @return Regeneration rate of the magnet field
     */
    int getMagnetRegenRate();

    /**
     *
     * @return EU Recipe multiplier applied by the magnet
     */

    int energyConsumption();

    /**
     * @return the {@link ResourceLocation} defining the base texture of the magnet
     */
    ResourceLocation getTexture();

    IMagnetType[] ALL_MAGNETS_CAPACITY_SORTED = CosmicCoreAPI.MAGNET_COILS.keySet().stream()
            .sorted(Comparator.comparing(IMagnetType::getMagnetFieldCapacity))
            .toArray(IMagnetType[]::new);

    @Nullable
    static IMagnetType getMinRequiredType(int minimumCapacity) {
        return Arrays.stream(ALL_MAGNETS_CAPACITY_SORTED)
                .filter(magnet -> magnet.getMagnetFieldCapacity() >= minimumCapacity)
                .findFirst().orElse(null);
    }
}
