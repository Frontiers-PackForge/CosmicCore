package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.gregtechceu.gtceu.common.block.CableBlock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;

@Mixin(value = CableBlock.class, remap = false)
public abstract class LosslessCableTooltipMixin {

    @WrapOperation(
                   method = "appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
                   slice = @Slice(
                                  from = @At(
                                             value = "INVOKE",
                                             target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/WireProperties;getLossPerBlock()I")),
                   require = 1,
                   expect = 1,
                   allow = 1)
    private boolean cosmiccore$hideLossLine(List<?> tooltip, Object line, Operation<Boolean> original) {
        return false;
    }
}
