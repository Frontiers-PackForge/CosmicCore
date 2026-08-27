package com.ghostipedia.cosmiccore.mixin.deployer;

import net.liukrast.deployer.lib.logistics.LogisticallyLinked;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FactoryPanelBehaviour.class)
public abstract class FactoryPanelLogisticallyLinkedMixin {

    @Definition(id = "heldItem", local = @Local(type = ItemStack.class, name = "heldItem"))
    @Definition(
                id = "getItem",
                method = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;")
    @Definition(id = "LogisticallyLinkedBlockItem", type = LogisticallyLinkedBlockItem.class)
    @Expression("heldItem.getItem() instanceof LogisticallyLinkedBlockItem")
    @ModifyExpressionValue(method = "onShortInteract", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean cosmiccore$recognizeLinkedPanelItems(boolean original,
                                                         @Local(name = "heldItem") ItemStack heldItem) {
        return original || heldItem.getItem() instanceof LogisticallyLinked;
    }
}
