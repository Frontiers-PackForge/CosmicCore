package com.ghostipedia.cosmiccore.gtbridge.pipenet.blockentities;


import com.ghostipedia.cosmiccore.gtbridge.machines.IPressureContainer;
import com.ghostipedia.cosmiccore.gtbridge.machines.MachineCaps;
import com.ghostipedia.cosmiccore.gtbridge.pipenet.PressurePipeData;
import com.ghostipedia.cosmiccore.gtbridge.pipenet.PressurePipeNet;
import com.ghostipedia.cosmiccore.gtbridge.pipenet.PressurePipeType;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class PressurePipeBlockEntity extends PipeBlockEntity<PressurePipeType, PressurePipeData> {
    protected WeakReference<PressurePipeNet> currentPressureNet = new WeakReference<>(null);


    public PressurePipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            BlockEntity be = level.getBlockEntity(getBlockPos().relative(side));
            if (be instanceof PressurePipeBlockEntity) {
                return false;
            }
            return be != null && be.getCapability(MachineCaps.CAPABILITY_PRESSURE_CONTAINER).isPresent();
        }
        return false;
    }

    @Nullable
    private PressurePipeNet getPressureNet() {
        if (level instanceof ServerLevel serverLevel && getBlockState().getBlock() instanceof PressurePipeBlock pressurePipeBlock) {
            PressurePipeNet currentPressureNet = this.currentPressureNet.get();
            if (currentPressureNet != null && currentPressureNet.isValid() && currentPressureNet.containsNode(getBlockPos()))
                return currentPressureNet; //return current net if it is still valid
            currentPressureNet = pressurePipeBlock.getWorldPipeNet(serverLevel).getNetFromPos(getBlockPos());
            if (currentPressureNet != null) {
                this.currentPressureNet = new WeakReference<>(currentPressureNet);
            }
        }
        return this.currentPressureNet.get();
    }

    @Nullable
    public IPressureContainer getPressureContainer(@Nullable Direction side) {
        if (side != null && isBlocked(side)) return null;
        if (isRemote()) return IPressureContainer.EMPTY;
        var PNet = getPressureNet();
        if (PNet != null) {
            return PNet;
        }
        return IPressureContainer.EMPTY;
    }
}
