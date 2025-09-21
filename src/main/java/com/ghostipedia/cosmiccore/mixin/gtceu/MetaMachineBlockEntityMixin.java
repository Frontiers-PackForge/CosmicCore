package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.EmberHatchPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import com.rekindled.embers.api.capabilities.EmbersCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(value = MetaMachineBlockEntity.class, remap = false)

public class MetaMachineBlockEntityMixin {

    // Specifically target the getCapability(Machine, Capability, Direction) method
    @Inject(method = "getCapability(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;",
            at = @At("TAIL"),
            cancellable = true)
    private static <T> void injectCapability(MetaMachine machine,
                                             @NotNull Capability<T> cap,
                                             @Nullable Direction side,
                                             CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (cap == EmbersCapabilities.EMBER_CAPABILITY) {
            if (machine instanceof EmberHatchPartMachine emberHatch) {
                cir.setReturnValue(EmbersCapabilities.EMBER_CAPABILITY.orEmpty(cap,
                        LazyOptional.of(() -> emberHatch.emberContainer.capability)));
                return;
            }

        }
        cir.setReturnValue(LazyOptional.empty());
    }
}
