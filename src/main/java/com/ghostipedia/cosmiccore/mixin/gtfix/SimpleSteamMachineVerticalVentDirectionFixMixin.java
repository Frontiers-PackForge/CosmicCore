package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;

import net.minecraft.core.Direction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = SimpleSteamMachine.class, remap = false)
public abstract class SimpleSteamMachineVerticalVentDirectionFixMixin {

    @ModifyArg(
               method = "updateModelVentDirection",
               at = @At(
                        value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/api/multiblock/util/RelativeDirection;findRelativeOf(Lnet/minecraft/core/Direction;Lnet/minecraft/core/Direction;Lnet/minecraft/core/Direction;)Lcom/gregtechceu/gtceu/api/multiblock/util/RelativeDirection;"),
               index = 2,
               require = 1)
    private Direction cosmiccore$useHorizontalVerticalFacingReference(Direction upwardsDirection) {
        if (upwardsDirection.getAxis() != Direction.Axis.Y) {
            return upwardsDirection;
        }
        SimpleSteamMachine machine = (SimpleSteamMachine) (Object) this;
        return machine.getFrontFacing() == Direction.UP ? Direction.SOUTH : Direction.NORTH;
    }
}
