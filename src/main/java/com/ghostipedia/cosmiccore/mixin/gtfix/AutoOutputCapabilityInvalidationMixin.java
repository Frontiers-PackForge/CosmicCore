package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AutoOutputTrait.class, remap = false)
public abstract class AutoOutputCapabilityInvalidationMixin {

    @Shadow
    protected @org.jetbrains.annotations.Nullable Direction itemOutputDirection;

    @Shadow
    protected @org.jetbrains.annotations.Nullable Direction fluidOutputDirection;

    @Shadow
    protected boolean allowItemInputFromOutputSide;

    @Shadow
    protected boolean allowFluidInputFromOutputSide;

    @Inject(
            method = "setAllowItemInputFromOutputSide",
            at = @At(
                     value = "FIELD",
                     target = "Lcom/gregtechceu/gtceu/common/machine/trait/AutoOutputTrait;allowItemInputFromOutputSide:Z",
                     opcode = Opcodes.PUTFIELD),
            require = 1)
    private void cosmiccore$invalidateItemAllowInput(boolean allow, CallbackInfo ci) {
        cosmiccore$invalidateCapabilities(allowItemInputFromOutputSide != allow);
    }

    @Inject(
            method = "setAllowFluidInputFromOutputSide",
            at = @At(
                     value = "FIELD",
                     target = "Lcom/gregtechceu/gtceu/common/machine/trait/AutoOutputTrait;allowFluidInputFromOutputSide:Z",
                     opcode = Opcodes.PUTFIELD),
            require = 1)
    private void cosmiccore$invalidateFluidAllowInput(boolean allow, CallbackInfo ci) {
        cosmiccore$invalidateCapabilities(allowFluidInputFromOutputSide != allow);
    }

    @Inject(
            method = "setItemOutputDirection",
            at = @At(
                     value = "FIELD",
                     target = "Lcom/gregtechceu/gtceu/common/machine/trait/AutoOutputTrait;itemOutputDirection:Lnet/minecraft/core/Direction;",
                     opcode = Opcodes.PUTFIELD),
            require = 1)
    private void cosmiccore$invalidateItemOutputDirection(Direction outputFacing, CallbackInfo ci) {
        cosmiccore$invalidateCapabilities(itemOutputDirection != outputFacing);
    }

    @Inject(
            method = "setFluidOutputDirection",
            at = @At(
                     value = "FIELD",
                     target = "Lcom/gregtechceu/gtceu/common/machine/trait/AutoOutputTrait;fluidOutputDirection:Lnet/minecraft/core/Direction;",
                     opcode = Opcodes.PUTFIELD),
            require = 1)
    private void cosmiccore$invalidateFluidOutputDirection(Direction outputFacing, CallbackInfo ci) {
        cosmiccore$invalidateCapabilities(fluidOutputDirection != outputFacing);
    }

    @Unique
    private void cosmiccore$invalidateCapabilities(boolean changed) {
        if (!changed) return;
        MetaMachine machine = ((AutoOutputTrait) (Object) this).getMachine();
        if (machine.getLevel() instanceof ServerLevel level) {
            level.invalidateCapabilities(machine.getBlockPos());
        }
    }
}
