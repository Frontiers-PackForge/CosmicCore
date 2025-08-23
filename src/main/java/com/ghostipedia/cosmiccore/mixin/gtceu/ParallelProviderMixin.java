package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.PCBFoundryMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.integration.jade.provider.ParallelProvider;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import snownee.jade.api.BlockAccessor;

@Debug(export = true)
@Mixin(value = ParallelProvider.class, remap = false)
public abstract class ParallelProviderMixin {

    @Inject(method = "appendServerData",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V",
                     ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true)
    public void cosmicCore$appendServerData(
                                            CompoundTag compoundTag,
                                            BlockAccessor blockAccessor,
                                            CallbackInfo ci) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof IParallelHatch parallelHatch) {
                if (parallelHatch instanceof ParallelHatchPartMachine multiParallelHatch) {
                    if (multiParallelHatch.getControllers().size() == 1) {
                        if (multiParallelHatch.getControllers().first() instanceof PCBFoundryMachine multiContoller) {

                            ci.cancel();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "appendServerData",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V",
                     ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true)
    public void cosmicCore$appendServerData2(CompoundTag compoundTag, BlockAccessor blockAccessor, CallbackInfo ci) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof IMultiController controller) {
                if (controller instanceof PCBFoundryMachine multiParallelHatch) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "appendServerData",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/machine/feature/multiblock/IMultiController;getParallelHatch()Ljava/util/Optional;"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true)
    public void cosmicCore$appendServerData3(CompoundTag compoundTag, BlockAccessor blockAccessor, CallbackInfo ci) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof IMultiController controller) {
                if (controller instanceof PCBFoundryMachine multiParallelHatch) {
                    ci.cancel();
                }
            }
        }
    }
}
