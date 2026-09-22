package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.misc.MachineRecipeLoader;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.block.Blocks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MachineRecipeLoader.class, remap = false)
public abstract class BlackstoneMaceratorRecipeFixMixin {

    @Inject(method = "registerRecyclingRecipes(Lnet/minecraft/data/recipes/RecipeOutput;)V",
            at = @At("TAIL"),
            require = 1)
    private static void cosmiccore$addBlackstoneMaceratorRecipe(RecipeOutput provider, CallbackInfo ci) {
        GTRecipeTypes.MACERATOR_RECIPES.recipeBuilder(CosmicCore.id("macerate_blackstone"))
                .inputItems(Blocks.BLACKSTONE.asItem())
                .outputItems(TagPrefix.dust, GTMaterials.Blackstone)
                .duration(150)
                .EUt(2)
                .save(provider);
    }
}
