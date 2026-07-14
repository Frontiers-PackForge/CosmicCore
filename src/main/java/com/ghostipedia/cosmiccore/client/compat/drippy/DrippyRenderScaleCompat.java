package com.ghostipedia.cosmiccore.client.compat.drippy;

import com.mojang.blaze3d.vertex.PoseStack;

public final class DrippyRenderScaleCompat {

    private DrippyRenderScaleCompat() {}

    public static void applyUntrackedScale(PoseStack poseStack, float x, float y, float z) {
        poseStack.last().pose().scale(x, y, z);
    }
}
