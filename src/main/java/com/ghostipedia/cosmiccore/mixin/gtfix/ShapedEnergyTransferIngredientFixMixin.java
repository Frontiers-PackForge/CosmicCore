package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.EnergyTransferRecipeIngredients;

import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.data.recipe.builder.ShapedEnergyTransferRecipeBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ShapedEnergyTransferRecipeBuilder.class, remap = false)
public abstract class ShapedEnergyTransferIngredientFixMixin {

    @Shadow
    protected Ingredient chargeIngredient;

    @Redirect(
              method = "define(CLnet/minecraft/world/item/ItemStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/ShapedEnergyTransferRecipeBuilder;",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/neoforged/neoforge/common/crafting/DataComponentIngredient;of(ZLnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/crafting/Ingredient;"))
    private Ingredient cosmiccore$matchChargeSourceWithoutTemplateEnergy(boolean strict, ItemStack template) {
        return EnergyTransferRecipeIngredients.of(strict, template, chargeIngredient,
                GTDataComponents.ENERGY_CONTENT.get());
    }
}
