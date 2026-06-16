package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The published gtceu codec requires a "groupColor" field on every recipe but
 * {@code GTRecipeSchema} (used by KubeJS) doesn't export a corresponding RecipeKey, so every
 * KubeJS-built recipe is rejected with {@code No key groupColor in MapLike[...]}. Inject the
 * default (-1, "no group") into the JSON before the codec sees it.
 */
@Mixin(value = GTRecipeSerializer.class, remap = false)
public abstract class GTRecipeSerializerMixin {

    @Inject(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;",
            at = @At("HEAD"))
    private void cosmiccore$defaultGroupColor(ResourceLocation id, JsonObject json,
                                              CallbackInfoReturnable<GTRecipe> cir) {
        if (!json.has("groupColor")) {
            json.addProperty("groupColor", -1);
        }
    }
}
