package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine$ExportOnlyAEStockingItemSlot",
       remap = false)
public abstract class MachineActivityAEStockingItemInputMixin {

    @WrapMethod(method = "extractItem")
    private ItemStack cosmiccore$input(int slot, int amount, boolean simulated, Operation<ItemStack> original) {
        return MachineActivityRuntime.recordedInput(original.call(slot, amount, simulated), simulated);
    }
}
