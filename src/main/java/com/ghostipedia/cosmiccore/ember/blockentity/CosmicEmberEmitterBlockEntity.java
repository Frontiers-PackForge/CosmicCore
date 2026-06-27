package com.ghostipedia.cosmiccore.ember.blockentity;

import com.ghostipedia.cosmiccore.ember.ICosmicEmberStats;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.api.capabilities.EmbersCapabilities;
import com.rekindled.embers.api.power.IEmberCapability;
import com.rekindled.embers.api.power.IEmberPacketReceiver;
import com.rekindled.embers.blockentity.EmberEmitterBlockEntity;
import com.rekindled.embers.compat.sublevel.SubLevelCompat;
import com.rekindled.embers.datagen.EmbersSounds;
import com.rekindled.embers.entity.EmberPacketEntity;
import com.rekindled.embers.util.CapabilityCompat;
import com.rekindled.embers.util.Misc;
import lombok.Getter;

import java.util.HashSet;

public class CosmicEmberEmitterBlockEntity extends EmberEmitterBlockEntity implements ICosmicEmberStats {

    @Getter
    private int tier;

    @Override
    public double transfer() {
        return 250 * Math.pow(4, tier);
    }

    @Override
    public double pull() {
        return 50 * Math.pow(4, tier);
    }

    public CosmicEmberEmitterBlockEntity(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState, int tier) {
        super(type, pPos, pBlockState);
        capability.setEmberCapacity(250 * Math.pow(4, tier + 2));
        this.tier = tier;
    }

    public static CosmicEmberEmitterBlockEntity create(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState,
                                                       int tier) {
        return new CosmicEmberEmitterBlockEntity(type, pPos, pBlockState, tier);
    }

    @Override
    public boolean canSendBurst() {
        validateRangeLimitedLink();
        if (level == null) {
            return false;
        }
        if (level.hasNeighborSignal(worldPosition) && target != null && !level.isClientSide) {
            return false;
        }
        BlockEntity targetTile = SubLevelCompat.findReachableLinkedTarget(
                this, target, targetSubLevelId, targetPhysicalPosition);
        if (targetTile == null) {
            if (rangeLimitedEndpoint && SubLevelCompat.isCrossSubLevelLink(this, targetSubLevelId)) {
                target = null;
                targetSubLevelId = null;
                targetTrackingPointId = null;
                targetPhysicalPosition = null;
                rangeLimitedEndpoint = false;
                setChanged();
            }
            return false;
        }
        if (!SubLevelCompat.isInSubLevel(this) && !SubLevelCompat.isInSubLevel(targetTile)) {
            if (!level.isLoaded(target)) {
                return false;
            }
            if (trajectoryChunks == null) {
                trajectoryChunks = new HashSet<>();
                Misc.calculateTrajectoryChunks(trajectoryChunks, worldPosition, target,
                        getEmittingDirection(level.getBlockState(worldPosition)
                                .getValue(BlockStateProperties.FACING)));
            }
            if (level instanceof ServerLevel serverLevel) {
                for (ChunkPos chunkPos : trajectoryChunks) {
                    if (!serverLevel.isNaturalSpawningAllowed(chunkPos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  CosmicEmberEmitterBlockEntity blockEntity) {
        blockEntity.ticksExisted++;
        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockEntity attachedTile = SubLevelCompat.findAdjacent(blockEntity, facing.getOpposite());
        if (blockEntity.ticksExisted % 5L == 0L && attachedTile != null) {
            IEmberCapability cap = CapabilityCompat
                    .getCapability(attachedTile, EmbersCapabilities.EMBER_CAPABILITY, facing).orElse(null);
            if (cap != null && cap.getEmber() > 0.0 &&
                    blockEntity.capability.getEmber() < blockEntity.capability.getEmberCapacity()) {
                double removed = cap.removeAmount(blockEntity.pull(), true);
                blockEntity.capability.addAmount(removed, true);
            }
        }
        if ((blockEntity.ticksExisted + (long) blockEntity.offset) % 20L == 0L && blockEntity.canSendBurst() &&
                blockEntity.capability.getEmber() > blockEntity.pull()) {
            BlockEntity targetTile = SubLevelCompat.findReachableLinkedTarget(
                    blockEntity, blockEntity.target, blockEntity.targetSubLevelId,
                    blockEntity.targetPhysicalPosition);
            if (targetTile instanceof IEmberPacketReceiver receiver && receiver.hasRoomFor(blockEntity.transfer())) {
                EmberPacketEntity packet = RegistryManager.EMBER_PACKET.get().create(blockEntity.level);
                Vec3 velocity = SubLevelCompat.toPhysicalDirection(blockEntity, getBurstVelocity(facing));
                Vec3 start = SubLevelCompat.toPhysicalPosition(blockEntity, Vec3.atCenterOf(pos));
                Vec3 destination = SubLevelCompat.currentTrackedPhysicalPosition(
                        blockEntity, blockEntity.target, blockEntity.targetSubLevelId,
                        blockEntity.targetPhysicalPosition);
                double sent = Math.min(blockEntity.transfer(), blockEntity.capability.getEmber());
                packet.initCustom(start, destination, velocity.x, velocity.y, velocity.z, sent);
                packet.pos = blockEntity.getBlockPos().immutable();
                packet.setTrackedTarget(blockEntity.target, blockEntity.targetSubLevelId);
                blockEntity.capability.removeAmount(sent, true);
                blockEntity.level.addFreshEntity(packet);
                level.playSound(null, pos, EmbersSounds.EMBER_EMIT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }
}
