package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.PCBFoundryMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.integration.jade.provider.ParallelProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import snownee.jade.api.BlockAccessor;

@Mixin(value = ParallelProvider.class, remap = false)
public abstract class ParallelProviderMixin {

    @Inject(method = "appendServerData", at = @At("HEAD"), cancellable = true)
    private void cosmicCore$skipStellarModule(CompoundTag compoundTag, BlockAccessor blockAccessor, CallbackInfo ci) {
        if (cosmicCore$shouldSkip(blockAccessor)) {
            ci.cancel();
        }
    }

    @Inject(method = "appendServerData",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V",
                     ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true,
            require = 0)
    private void cosmicCore$skipPCBFoundryHatch(CompoundTag compoundTag, BlockAccessor blockAccessor, CallbackInfo ci) {
        if (cosmicCore$isPCBFoundryParallelHatch(blockAccessor)) {
            ci.cancel();
        }
    }

    @Inject(method = "appendServerData",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V",
                     ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true,
            require = 0)
    private void cosmicCore$skipPCBFoundryController(CompoundTag compoundTag, BlockAccessor blockAccessor,
                                                     CallbackInfo ci) {
        if (cosmicCore$isPCBFoundryController(blockAccessor)) {
            ci.cancel();
        }
    }

    @Inject(method = "appendServerData",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/machine/feature/multiblock/MultiblockControllerMachine;getParallelHatch()Ljava/util/Optional;"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true,
            require = 0)
    private void cosmicCore$skipPCBFoundryGetParallel(CompoundTag compoundTag, BlockAccessor blockAccessor,
                                                      CallbackInfo ci) {
        if (cosmicCore$isPCBFoundryController(blockAccessor)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean cosmicCore$shouldSkip(BlockAccessor blockAccessor) {
        MetaMachine machine = cosmicCore$getMachine(blockAccessor);
        return machine instanceof StellarBaseModule;
    }

    @Unique
    private boolean cosmicCore$isPCBFoundryController(BlockAccessor blockAccessor) {
        MetaMachine machine = cosmicCore$getMachine(blockAccessor);
        return machine instanceof PCBFoundryMachine;
    }

    @Unique
    private boolean cosmicCore$isPCBFoundryParallelHatch(BlockAccessor blockAccessor) {
        MetaMachine machine = cosmicCore$getMachine(blockAccessor);
        if (machine instanceof IParallelHatch && machine instanceof ParallelHatchPartMachine hatch) {
            if (hatch.getControllers().size() == 1) {
                return hatch.getControllers().first() instanceof PCBFoundryMachine;
            }
        }
        return false;
    }

    @Unique
    private MetaMachine cosmicCore$getMachine(BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            return blockEntity.getMetaMachine();
        }
        return null;
    }
}
