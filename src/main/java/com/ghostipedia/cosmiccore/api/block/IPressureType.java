package com.ghostipedia.cosmiccore.api.block;

import com.ghostipedia.cosmiccore.api.CosmicCoreAPI;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public interface IPressureType {

        /**
         * @return The Unique Name of the Pressure Vessel
         */
        @NotNull
        String getName();

        /**
         *
         * @return Returns the Max Vessel Capacity Size
         */
        int getPressureVesselCapacity();

        /**
         *
         * @return Regeneration rate of the magnet field
         */
        int getVarietyMultiplier();


        /**
         * @return the {@link ResourceLocation} defining the base texture of the magnet
         */
        ResourceLocation getTexture();
        com.ghostipedia.cosmiccore.api.block.IPressureType[] ALL_PRESSURE_VESSELS_SORTED = CosmicCoreAPI.PRESSURE_VESSELS.keySet().stream()
                .sorted(Comparator.comparing(com.ghostipedia.cosmiccore.api.block.IPressureType::getPressureVesselCapacity))
                .toArray(com.ghostipedia.cosmiccore.api.block.IPressureType[]::new);

        @Nullable
        static com.ghostipedia.cosmiccore.api.block.IPressureType getMinRequiredType(int minimumVesselSize) {
            return Arrays.stream(ALL_PRESSURE_VESSELS_SORTED)
                    .filter(vessel -> vessel.getPressureVesselCapacity() >= minimumVesselSize)
                    .findFirst().orElse(null);
        }
    }

