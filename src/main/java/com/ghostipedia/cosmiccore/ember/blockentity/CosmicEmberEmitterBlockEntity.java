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
import com.rekindled.embers.datagen.EmbersSounds;
import com.rekindled.embers.entity.EmberPacketEntity;
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

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  CosmicEmberEmitterBlockEntity blockEntity) {
        blockEntity.ticksExisted++;
        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockEntity attachedTile = level.getBlockEntity(pos.relative(facing, -1));
        if (blockEntity.ticksExisted % 5 == 0 && attachedTile != null) {
            IEmberCapability cap = level.getCapability(EmbersCapabilities.EMBER_BLOCK_CAPABILITY,
                    pos.relative(facing, -1), facing);
            if (cap != null) {
                if (cap.getEmber() > 0 &&
                        blockEntity.capability.getEmber() < blockEntity.capability.getEmberCapacity()) {
                    double removed = cap.removeAmount(blockEntity.pull(), true);
                    blockEntity.capability.addAmount(removed, true);
                }
            }
        }
        if ((blockEntity.ticksExisted + blockEntity.offset) % 20 == 0 && blockEntity.canSendBurst() &&
                blockEntity.capability.getEmber() > PULL_RATE) {
            BlockEntity targetTile = level.getBlockEntity(blockEntity.target);
            if (targetTile instanceof IEmberPacketReceiver) {
                if (((IEmberPacketReceiver) targetTile).hasRoomFor(blockEntity.transfer())) {
                    EmberPacketEntity packet = RegistryManager.EMBER_PACKET.get().create(blockEntity.level);
                    Vec3 velocity = getBurstVelocity(facing);
                    packet.initCustom(pos, blockEntity.target, velocity.x, velocity.y, velocity.z,
                            Math.min(blockEntity.transfer(), blockEntity.capability.getEmber()));
                    blockEntity.capability
                            .removeAmount(Math.min(blockEntity.transfer(), blockEntity.capability.getEmber()), true);
                    blockEntity.level.addFreshEntity(packet);
                    level.playSound(null, pos, EmbersSounds.EMBER_EMIT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
    }

    @Override
    public boolean canSendBurst() {
        if (target != null && level.isLoaded(target) && !level.isClientSide) {
            if (trajectoryChunks == null) {
                trajectoryChunks = new HashSet<ChunkPos>();
                Misc.calculateTrajectoryChunks(trajectoryChunks, worldPosition, target,
                        getEmittingDirection(level.getBlockState(worldPosition).getValue(BlockStateProperties.FACING)));
            }
            if (level instanceof ServerLevel serverLevel) {
                for (ChunkPos chunk : trajectoryChunks) {
                    if (!serverLevel.isNaturalSpawningAllowed(chunk))
                        return false;
                }
            }
            return true;
        }
        return false;
    }
}
