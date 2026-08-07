package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(value = ToolHelper.class, remap = false)
public abstract class HardHammerDropConversionFixMixin {

    @WrapOperation(
                   method = "applyHammerDropConversion(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;IFLnet/minecraft/util/RandomSource;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/recipe/RecipeHelper;handleRecipeIO(Lcom/gregtechceu/gtceu/api/capability/recipe/IRecipeCapabilityHolder;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;Ljava/util/Map;)Lcom/gregtechceu/gtceu/api/recipe/ActionResult;",
                            remap = false),
                   require = 1,
                   remap = false)
    private static ActionResult cosmiccore$useLoopRecipe(IRecipeCapabilityHolder holder, GTRecipe ignored, IO io,
                                                         Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                                                         Operation<ActionResult> original,
                                                         @Local(ordinal = 1) GTRecipe recipe) {
        return original.call(holder, recipe, io, chanceCaches);
    }

    @WrapOperation(
                   method = "applyHammerDropConversion(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;IFLnet/minecraft/util/RandomSource;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                            ordinal = 0),
                   require = 1)
    private static boolean cosmiccore$addCopiedNonOreDrop(List<ItemStack> drops, Object value,
                                                          Operation<Boolean> original,
                                                          @Local(argsOnly = true) float dropChance,
                                                          @Local(argsOnly = true) RandomSource random) {
        if (!cosmiccore$passesDropChance(dropChance, random)) return false;
        return original.call(drops, ((ItemStack) value).copy());
    }

    @WrapOperation(
                   method = "applyHammerDropConversion(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/List;IFLnet/minecraft/util/RandomSource;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                            ordinal = 1),
                   require = 1)
    private static boolean cosmiccore$addOreDropWithChance(List<ItemStack> drops, Object value,
                                                           Operation<Boolean> original,
                                                           @Local(argsOnly = true) float dropChance,
                                                           @Local(argsOnly = true) RandomSource random) {
        if (!cosmiccore$passesDropChance(dropChance, random)) return false;
        return original.call(drops, value);
    }

    @Unique
    private static boolean cosmiccore$passesDropChance(float dropChance, RandomSource random) {
        return dropChance >= 1.0F || random.nextFloat() <= dropChance;
    }
}
