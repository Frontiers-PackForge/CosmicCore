package com.ghostipedia.cosmiccore.common.transmission.geometry;

import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public record TwinWireSample(Vec3 positiveLateral, Vec3 negativeLateral) {

    public TwinWireSample {
        Objects.requireNonNull(positiveLateral);
        Objects.requireNonNull(negativeLateral);
    }
}
