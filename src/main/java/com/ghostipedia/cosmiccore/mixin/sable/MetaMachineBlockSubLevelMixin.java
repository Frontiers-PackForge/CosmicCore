package com.ghostipedia.cosmiccore.mixin.sable;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.integration.sable.SableAssemblyRotation;
import com.ghostipedia.cosmiccore.integration.sable.SableAssemblyRotationHolder;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MetaMachineBlock.class, remap = false)
public abstract class MetaMachineBlockSubLevelMixin implements BlockSubLevelAssemblyListener {

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState state, BlockPos oldPos,
                          BlockPos newPos) {
        if (MetaMachine.getMachine(resultingLevel, newPos) instanceof MetaMachine machine) {
            try {
                SableAssemblyRotation.rotateMachine(machine.getCoverContainer(),
                        SableAssemblyRotationHolder.current(), resultingLevel.registryAccess());
            } catch (Throwable t) {
                CosmicCore.LOGGER.error("Failed to rotate machine covers during Sable assembly at {}", newPos, t);
            }
            machine.getSyncDataHolder().resyncAllFields();
            machine.scheduleRenderUpdate();
        }
    }
}
