package com.ghostipedia.cosmiccore.client.renderer.deployment;

import net.minecraft.core.BlockPos;

public interface LeylineEncoderPreviewAccess {

    void cosmiccore$selectFacingTarget(BlockPos pos);

    default boolean cosmiccore$usesCompactControls() {
        return true;
    }
}
