package com.ghostipedia.cosmiccore.mixin.qualityfoodironfurnaces;

import com.ghostipedia.cosmiccore.common.compat.qualityfood.QualityFoodCompat;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase", remap = false)
public abstract class IronFurnaceQualityOutputMixin {

    @ModifyExpressionValue(
                           method = { "canSmelt", "smeltItem", "smeltItemMult" },
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lnet/minecraft/world/item/crafting/Recipe;getResultItem(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"),
                           require = 3)
    private ItemStack cosmiccore$qualityNormalOutput(ItemStack result, RecipeHolder<?> recipe) {
        return cosmiccore$applyQuality(result, recipe, 0);
    }

    @ModifyExpressionValue(
                           method = { "canFactorySmelt", "smeltFactoryItem", "smeltFactoryItemMult" },
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lnet/minecraft/world/item/crafting/Recipe;getResultItem(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"),
                           require = 3)
    private ItemStack cosmiccore$qualityFactoryOutput(ItemStack result, RecipeHolder<?> recipe, int inputSlot) {
        return cosmiccore$applyQuality(result, recipe, inputSlot);
    }

    @ModifyExpressionValue(
                           method = "smeltItemMult",
                           at = @At(value = "NEW", target = "net/minecraft/world/item/ItemStack", ordinal = 0))
    private ItemStack cosmiccore$qualityNormalBatchOutput(ItemStack result, RecipeHolder<?> recipe) {
        return cosmiccore$applyQuality(result, recipe, 0);
    }

    @ModifyExpressionValue(
                           method = "smeltFactoryItemMult",
                           at = @At(value = "NEW", target = "net/minecraft/world/item/ItemStack", ordinal = 0))
    private ItemStack cosmiccore$qualityFactoryBatchOutput(ItemStack result, RecipeHolder<?> recipe, int inputSlot) {
        return cosmiccore$applyQuality(result, recipe, inputSlot);
    }

    private ItemStack cosmiccore$applyQuality(ItemStack result, RecipeHolder<?> recipe, int inputSlot) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level == null) return result;
        ItemStack input = ((Container) this).getItem(inputSlot);
        return QualityFoodCompat.recipeOutput(result, input);
    }
}
