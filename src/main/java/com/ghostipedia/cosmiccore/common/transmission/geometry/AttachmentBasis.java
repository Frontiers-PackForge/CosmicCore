package com.ghostipedia.cosmiccore.common.transmission.geometry;

import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public record AttachmentBasis(Vec3 up, Vec3 lateralHint) {

    public AttachmentBasis {
        Objects.requireNonNull(up);
        Objects.requireNonNull(lateralHint);
        if (up.lengthSqr() < 1.0E-12 || lateralHint.lengthSqr() < 1.0E-12) {
            throw new IllegalArgumentException("Attachment basis vectors must be non-zero");
        }
        up = up.normalize();
        lateralHint = lateralHint.normalize();
    }

    public static AttachmentBasis worldAligned() {
        return new AttachmentBasis(new Vec3(0.0, 1.0, 0.0), new Vec3(1.0, 0.0, 0.0));
    }
}
