package com.ghostipedia.cosmiccore.mixin.gttweak.activity;

import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;

import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ExportOnlyAEItemSlot.class, remap = false)
public abstract class MachineActivityAEItemInputMixin {

    @WrapMethod(method = "extractItem")
    private ItemStack cosmiccore$input(int slot, int amount, boolean simulated, Operation<ItemStack> original) {
        return MachineActivityRuntime.recordedInput(original.call(slot, amount, simulated), simulated);
    }
}
