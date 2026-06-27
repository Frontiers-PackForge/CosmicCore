package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MetaMachineBlock.class, remap = false)
public class MetaMachineBlockNoMoveDropMixin {

    @Redirect(
              method = "onRemove",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/machine/MetaMachine;onMachineDestroyed()V"),
              require = 1)
    private void cosmiccore$skipDropOnMove(MetaMachine machine,
                                           BlockState pState, Level pLevel, BlockPos pPos,
                                           BlockState pNewState, boolean pIsMoving) {
        if (!pIsMoving) {
            machine.onMachineDestroyed();
        }
    }
}
