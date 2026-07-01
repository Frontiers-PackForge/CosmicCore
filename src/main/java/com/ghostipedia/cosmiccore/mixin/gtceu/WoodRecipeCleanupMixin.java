package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.data.recipe.WoodFormRemovals;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class WoodRecipeCleanupMixin {

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    private void cosmiccore$removeCheapWoodRecipes(Map<ResourceLocation, JsonElement> map,
                                                   ResourceManager resourceManager,
                                                   ProfilerFiller profiler, CallbackInfo ci) {
        map.entrySet().removeIf(entry -> WoodFormRemovals.isCheapWoodFormRecipe(entry.getKey(), entry.getValue()));
    }
}
