package com.ghostipedia.cosmiccore.common.deployment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class LeylineDeploymentTarget {

    public static final double MAX_REACH = 96;

    private LeylineDeploymentTarget() {}

    public static BlockHitResult trace(Player player, float partialTick) {
        return trace(player, partialTick, MAX_REACH);
    }

    public static BlockHitResult trace(Player player, float partialTick, double reach) {
        Vec3 eye = player.getEyePosition(partialTick);
        return player.level().clip(new ClipContext(eye,
                eye.add(player.getViewVector(partialTick).scale(reach)), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, player));
    }

    public static BlockPos anchor(Player player, BlockHitResult hit) {
        return player.level().getBlockState(hit.getBlockPos()).canBeReplaced() ? hit.getBlockPos() :
                hit.getBlockPos().relative(hit.getDirection());
    }
}
