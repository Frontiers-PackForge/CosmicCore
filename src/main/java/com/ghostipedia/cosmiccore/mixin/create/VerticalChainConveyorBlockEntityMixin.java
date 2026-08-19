package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.VerticalChainDirection;
import com.ghostipedia.cosmiccore.common.compat.create.VerticalChainGeometry;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(value = ChainConveyorBlockEntity.class, remap = false)
public abstract class VerticalChainConveyorBlockEntityMixin implements VerticalChainDirection {

    @Unique
    private static final String COSMICCORE_VERTICAL_CHAIN_DIRECTION = "CosmicCoreVerticalChainDirection";

    @Shadow
    public Map<BlockPos, ConnectionStats> connectionStats;

    @Shadow
    public Set<BlockPos> connections;

    @Unique
    private int cosmiccore$verticalDirection;

    @Override
    public int cosmiccore$getVerticalDirection() {
        return cosmiccore$verticalDirection;
    }

    @Override
    public void cosmiccore$setVerticalDirection(int direction) {
        cosmiccore$verticalDirection = Math.floorMod(direction, 4);
        ChainConveyorBlockEntity blockEntity = (ChainConveyorBlockEntity) (Object) this;
        blockEntity.connectionStats = null;
        blockEntity.notifyUpdate();
    }

    @Override
    public void cosmiccore$cycleVerticalDirection() {
        cosmiccore$setVerticalDirection(cosmiccore$verticalDirection + 1);
        ChainConveyorBlockEntity blockEntity = (ChainConveyorBlockEntity) (Object) this;
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        for (BlockPos offset : connections) {
            if (!VerticalChainGeometry.requiresCustomStats(Vec3.atLowerCornerOf(offset))) {
                continue;
            }
            if (level.getBlockEntity(
                    blockEntity.getBlockPos().offset(offset)) instanceof VerticalChainDirection verticalChain) {
                verticalChain.cosmiccore$setVerticalDirection(cosmiccore$verticalDirection);
            }
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void cosmiccore$writeVerticalDirection(
                                                   CompoundTag tag, HolderLookup.Provider registries,
                                                   boolean clientPacket, CallbackInfo ci) {
        if (cosmiccore$verticalDirection != 0) {
            tag.putInt(COSMICCORE_VERTICAL_CHAIN_DIRECTION, cosmiccore$verticalDirection);
        }
    }

    @Inject(method = "read", at = @At("HEAD"))
    private void cosmiccore$readVerticalDirection(
                                                  CompoundTag tag, HolderLookup.Provider registries,
                                                  boolean clientPacket, CallbackInfo ci) {
        cosmiccore$verticalDirection = Math.floorMod(tag.getInt(COSMICCORE_VERTICAL_CHAIN_DIRECTION), 4);
    }

    @Redirect(
              method = "forPointsAlongChains",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/world/phys/Vec3;cross(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 cosmiccore$stabilizeVerticalCrossProduct(Vec3 direction, Vec3 axis) {
        return VerticalChainGeometry.stableCross(direction, axis);
    }

    @Inject(method = "calculateConnectionStats", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$calculateVerticalConnectionStats(BlockPos connection, CallbackInfo ci) {
        Vec3 offset = Vec3.atLowerCornerOf(connection);
        if (!VerticalChainGeometry.requiresCustomStats(offset)) {
            return;
        }

        ChainConveyorBlockEntity blockEntity = (ChainConveyorBlockEntity) (Object) this;
        boolean reversed = blockEntity.getSpeed() < 0;
        double horizontalDistance = offset.multiply(1, 0, 1).length();
        boolean pureVertical = horizontalDistance <= 0.5;
        float branchAngle = 80;
        float loopRadius = 0.72f;
        float forwardNudge = -0.155f;
        float baseDirection = pureVertical ? cosmiccore$verticalDirection * 90f :
                Mth.RAD_TO_DEG * (float) Mth.atan2(connection.getX(), connection.getZ());
        float angle;
        if (pureVertical) {
            float side = connection.getY() > 0 ? 1 : -1;
            angle = blockEntity.wrapAngle(baseDirection + branchAngle * side);
        } else {
            angle = blockEntity.wrapAngle(baseDirection - branchAngle * (reversed ? -1 : 1));
        }

        Vec3 nudge = VecHelper.rotate(new Vec3(0, 0, forwardNudge), baseDirection, Axis.Y);
        Vec3 start = Vec3.atBottomCenterOf(blockEntity.getBlockPos())
                .add(VecHelper.rotate(new Vec3(0, 0, loopRadius), angle, Axis.Y))
                .add(nudge)
                .add(0, 6 / 16f, 0);
        Vec3 end = Vec3.atBottomCenterOf(blockEntity.getBlockPos().offset(connection))
                .add(VecHelper.rotate(new Vec3(0, 0, loopRadius), angle, Axis.Y))
                .add(nudge)
                .add(0, 6 / 16f, 0);
        float length = (float) start.distanceTo(end);
        connectionStats.put(connection, new ConnectionStats(angle, length, start, end));
        ci.cancel();
    }
}
