package com.ghostipedia.cosmiccore.common.transmission.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public record PowerTowerNode(UUID id, BlockPos controllerPos, Vec3 wireAttachmentCenter, PowerTowerRole role,
                             @Nullable UUID ownerId, int terminalVoltageTier, boolean structureOperational) {

    public PowerTowerNode {
        Objects.requireNonNull(id);
        controllerPos = Objects.requireNonNull(controllerPos).immutable();
        Objects.requireNonNull(wireAttachmentCenter);
        if (!Double.isFinite(wireAttachmentCenter.x) || !Double.isFinite(wireAttachmentCenter.y) ||
                !Double.isFinite(wireAttachmentCenter.z))
            throw new IllegalArgumentException("Power tower attachment center must be finite");
        Objects.requireNonNull(role);
        if (role == PowerTowerRole.DUMMY && terminalVoltageTier != -1)
            throw new IllegalArgumentException("Dummy power towers cannot declare a terminal tier");
        if (role == PowerTowerRole.TERMINAL && terminalVoltageTier < 0)
            throw new IllegalArgumentException("Terminal power towers require a voltage tier");
    }
}
