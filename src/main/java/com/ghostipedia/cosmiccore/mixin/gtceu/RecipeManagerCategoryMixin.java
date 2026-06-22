package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Backport of GregTech-Modern PR #4683 to 1.21.
 * <p>
 * On /reload GregTech never clears {@link GTRecipeType}'s categoryMap, so EMI/JEI/REI accumulate stale
 * recipes (the RecipeDB rework that replaced GTRecipeLookup dropped the categoryMap.clear() it used to do,
 * and the 1.21 {@code beginStagingRecipes()} meant to restore it is dead code with no caller). We clear every
 * GTRecipeType's categoryMap at the head of RecipeManager#apply, before the XEI integrations repopulate it
 * via category.addRecipe()/buildRepresentativeRecipes(), so categories rebuild cleanly each reload.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerCategoryMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    private void cosmiccore$clearGtCategoryMaps(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                                ProfilerFiller profiler, CallbackInfo ci) {
        for (RecipeType<?> type : BuiltInRegistries.RECIPE_TYPE) {
            if (type instanceof GTRecipeType gtRecipeType) {
                gtRecipeType.getCategoryMap().clear();
            }
        }
    }
}
