package com.ghostipedia.cosmiccore.api.machine.part;

import com.ghostipedia.cosmiccore.common.data.CosmicMachines;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SteamFluidHatchPartMachine extends FluidHatchPartMachine {

    public SteamFluidHatchPartMachine(BlockEntityCreationInfo holder, IO io, long initialCapacity, int slots,
                                      Object... args) {
        super(holder, 1, io, 2000, 1, args);
    }

    @Override
    public boolean swapIO() {
        BlockPos blockPos = getHolder().pos();
        MachineDefinition newDefinition = null;

        if (io == IO.IN) {
            newDefinition = CosmicMachines.STEAM_EXPORT_HATCH;
        } else if (io == IO.OUT) {
            newDefinition = CosmicMachines.STEAM_IMPORT_HATCH;
        }
        if (newDefinition == null) return false;

        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof BlockEntityCreationInfo newHolder) {
            if (newHolder.getMetaMachine() instanceof SteamFluidHatchPartMachine newMachine) {
                newMachine.setFrontFacing(this.getFrontFacing());
                newMachine.setUpwardsFacing(this.getUpwardsFacing());
                newMachine.setPaintingColor(this.getPaintingColor());
                for (int i = 0; i < this.tank.getTanks(); i++) {
                    newMachine.tank.setFluidInTank(i, this.tank.getFluidInTank(i));
                }
            }
        }
        return true;
    }
}
