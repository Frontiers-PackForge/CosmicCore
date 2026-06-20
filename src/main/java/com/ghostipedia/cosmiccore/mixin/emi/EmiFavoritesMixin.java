package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.CosmicFavorite;
import com.ghostipedia.cosmiccore.integration.emi.CosmicRecipeFavorite;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Persists CosmicFavorite amounts through EMI's save/load cycle,
 * and hooks into EMI's save/load to trigger our bookmark group persistence.
 */
@Mixin(value = EmiFavorites.class, remap = false)
public class EmiFavoritesMixin {

    @Shadow
    public static List<EmiFavorite> favorites;

    @Inject(method = "save", at = @At("RETURN"))
    private static void cosmiccore$saveAmounts(CallbackInfoReturnable<JsonArray> cir) {
        JsonArray arr = cir.getReturnValue();
        if (arr == null) return;

        for (int i = 0; i < favorites.size() && i < arr.size(); i++) {
            EmiFavorite fav = favorites.get(i);
            JsonElement el = arr.get(i);
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();

            if (fav instanceof CosmicRecipeFavorite recipe) {
                obj.addProperty("cosmicType", "recipe");
                obj.addProperty("cosmicOutputAmount", recipe.getOutputAmount());

                JsonArray inputsArr = new JsonArray();
                for (CosmicRecipeFavorite.InputEntry input : recipe.getInputs()) {
                    JsonObject inputObj = new JsonObject();
                    inputObj.add("stack", EmiIngredientSerializer.getSerialized(input.stack()));
                    inputObj.addProperty("amount", input.amount());
                    inputsArr.add(inputObj);
                }
                obj.add("cosmicInputs", inputsArr);
            } else if (fav instanceof CosmicFavorite cosmic) {
                obj.addProperty("cosmicAmount", cosmic.getAmount());
            }
        }

        CosmicBookmarkManager.getInstance().save();
    }

    @Inject(method = "load", at = @At("RETURN"))
    private static void cosmiccore$loadAmounts(JsonArray arr, CallbackInfo ci) {
        CosmicBookmarkManager.getInstance().load();

        if (arr == null) return;

        for (int i = 0; i < arr.size() && i < favorites.size(); i++) {
            JsonElement el = arr.get(i);
            if (!el.isJsonObject()) continue;

            JsonObject obj = el.getAsJsonObject();
            EmiFavorite existing = favorites.get(i);

            if (obj.has("cosmicType") && "recipe".equals(obj.get("cosmicType").getAsString())) {
                long outputAmount = obj.has("cosmicOutputAmount") ? obj.get("cosmicOutputAmount").getAsLong() : 1;
                List<CosmicRecipeFavorite.InputEntry> inputs = new ArrayList<>();

                if (obj.has("cosmicInputs")) {
                    for (JsonElement inputEl : obj.getAsJsonArray("cosmicInputs")) {
                        if (!inputEl.isJsonObject()) continue;
                        JsonObject inputObj = inputEl.getAsJsonObject();
                        if (!inputObj.has("stack")) continue;

                        EmiIngredient stack = EmiIngredientSerializer.getDeserialized(inputObj.get("stack"));
                        long amount = inputObj.has("amount") ? inputObj.get("amount").getAsLong() : 1;
                        if (!stack.isEmpty()) {
                            inputs.add(new CosmicRecipeFavorite.InputEntry(stack, amount));
                        }
                    }
                }

                favorites.set(i, new CosmicRecipeFavorite(existing.getStack(), outputAmount, inputs));
            } else if (obj.has("cosmicAmount")) {
                long amount = obj.get("cosmicAmount").getAsLong();
                favorites.set(i, new CosmicFavorite(existing.getStack(), amount));
            }
        }
    }
}
