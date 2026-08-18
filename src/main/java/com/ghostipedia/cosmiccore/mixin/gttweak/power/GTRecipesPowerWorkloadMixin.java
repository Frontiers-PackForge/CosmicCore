package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.recipe.PowerRecipeVoltageConverter;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GTRecipes.class, remap = false)
public abstract class GTRecipesPowerWorkloadMixin {

    @ModifyVariable(method = "recipeAddition(Lnet/minecraft/data/recipes/RecipeOutput;)V",
                    at = @At("HEAD"),
                    argsOnly = true,
                    require = 1,
                    expect = 1,
                    allow = 1)
    private static RecipeOutput cosmiccore$applyNativePowerWorkloads(RecipeOutput original) {
        return new RecipeOutput() {

            @Override
            public Advancement.@NotNull Builder advancement() {
                return original.advancement();
            }

            @Override
            public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe,
                               @Nullable AdvancementHolder advancement, ICondition @NotNull... conditions) {
                if (recipe instanceof GTRecipe gtRecipe) {
                    PowerRecipeVoltageConverter.apply(gtRecipe);
                }
                original.accept(id, recipe, advancement, conditions);
            }
        };
    }
}
