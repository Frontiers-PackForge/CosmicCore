package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = IntCircuitBehaviour.class, remap = false)
public abstract class IntCircuitGhostSlotFixMixin {

    @ModifyArg(
               method = "stack(II)Lnet/minecraft/world/item/ItemStack;",
               at = @At(
                        value = "INVOKE",
                        target = "Lcom/tterrag/registrate/util/entry/ItemEntry;asStack(I)Lnet/minecraft/world/item/ItemStack;",
                        remap = false),
               index = 0,
               require = 1,
               remap = false)
    private static int cosmiccore$materializeGhostCircuit(int count) {
        return Math.max(1, count);
    }
}
