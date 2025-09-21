package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.abyss.AbyssBudgetCap;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.AbyssWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
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



    @Inject(method = "getCapability", at = @At("TAIL"), cancellable = true)
    public  @NotNull <T> LazyOptional<T> injectCapability(MetaMachine machine,
                                                       @NotNull Capability<T> cap,
                                                       @Nullable Direction side) {
        if (cap == AbyssBudgetCap.CAP) {
            if (machine instanceof AbyssWorkableElectricMultiblockMachine abyssMachine) {
                return AbyssBudgetCap.CAP.orEmpty(cap, LazyOptional.of(() -> abyssMachine));
            }

        }
        return null;
    }

}
