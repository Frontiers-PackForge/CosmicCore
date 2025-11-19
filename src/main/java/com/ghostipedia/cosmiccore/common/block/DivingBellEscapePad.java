package com.ghostipedia.cosmiccore.common.block;

import com.ghostipedia.cosmiccore.common.abyss.AbyssRules;
import com.ghostipedia.cosmiccore.common.teleporter.SafeTeleporter;
import com.ghostipedia.cosmiccore.common.teleporter.TeleportOriginCap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

// Escape Pad block that teleports players back to their origin dimension. Placed automatically by the Diving Bell.
public class DivingBellEscapePad extends Block {

    public DivingBellEscapePad(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.getCapability(TeleportOriginCap.CAP).ifPresent(cap -> {
            if (!cap.hasValidOrigin()) {
                // No valid origin data - send to respawn point
                serverPlayer.displayClientMessage(
                        Component.translatable("cosmiccore.divingbell.no_return"), true);
                teleportToRespawn(serverPlayer);
                return;
            }

            // Get return destination
            var originDim = cap.getOriginDimension();
            Vec3 originPos = cap.getOriginPosition();
            float originYaw = cap.getOriginYaw();
            float originPitch = cap.getOriginPitch();

            MinecraftServer server = level.getServer();
            if (server == null) return;

            ServerLevel originLevel = server.getLevel(originDim);
            if (originLevel == null) {
                // Origin dimension doesn't exist
                serverPlayer.displayClientMessage(
                        Component.translatable("cosmiccore.divingbell.invalid_origin"), true);
                cap.clearOriginData();
                teleportToRespawn(serverPlayer);
                return;
            }

            // Validate origin is safe (chunk loaded, not void)
            BlockPos originBlockPos = BlockPos.containing(originPos);
            if (!isOriginSafe(originLevel, originBlockPos)) {
                serverPlayer.displayClientMessage(
                        Component.translatable("cosmiccore.divingbell.unsafe_origin"), true);
                cap.clearOriginData();
                teleportToRespawn(serverPlayer);
                return;
            }

            // Teleport back to origin
            serverPlayer.changeDimension(originLevel, new SafeTeleporter(originBlockPos));

            // Restore rotation
            serverPlayer.setYRot(originYaw);
            serverPlayer.setXRot(originPitch);

            // Clear Abyss decay flag
            // Don't think this is needed...
//            serverPlayer.getCapability(com.ghostipedia.cosmiccore.common.abyss.AbyssBudgetCap.CAP)
//                    .ifPresent(abyssCap -> {
//                        abyssCap.setDecaying(AbyssRules.DIM, false);
//                    });

            // Success message
            serverPlayer.displayClientMessage(
                    Component.translatable("cosmiccore.divingbell.returned"), true);

            // Clear origin data
            cap.clearOriginData();
        });

        return InteractionResult.CONSUME;
    }

    // Check if the origin position is safe to teleport to.
    private boolean isOriginSafe(ServerLevel level, BlockPos pos) {
        // Check if chunk is loaded
        if (!level.isLoaded(pos)) {
            return false;
        }

        // Check if it's not in void
        if (pos.getY() < level.getMinBuildHeight()) {
            return false;
        }

        // Check 2-block tall air column where player stands
        BlockState at = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());

//        if (at.isSuffocating(level, pos)) {
//            return false;
//        }
//
//        if (!at.getFluidState().isEmpty()) {
//            return false;
//        }

        // Head level: same checks to ensure full 2-block clearance
        if (above.isSuffocating(level, pos.above())) {
            return false;
        }

        if (!above.getFluidState().isEmpty()) {
            return false;
        }

        return true;
    }

    // Teleport player to their respawn point as fallback.
    private void teleportToRespawn(ServerPlayer player) {
        BlockPos respawn = player.getRespawnPosition();
        ServerLevel respawnLevel = player.server.getLevel(player.getRespawnDimension());

        if (respawn != null && respawnLevel != null) {
            // Teleport to bed/respawn anchor
            player.teleportTo(respawnLevel, respawn.getX() + 0.5, respawn.getY(), respawn.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
        } else {
            // Ultimate fallback - overworld spawn (should not fail under normal circumstances)
            ServerLevel overworld = player.server.overworld();
            BlockPos spawn = overworld.getSharedSpawnPos();
            player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
        }
    }
}
