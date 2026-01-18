package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.CosmicFavorite;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Persists CosmicFavorite amounts through EMI's save/load cycle.
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
            if (favorites.get(i) instanceof CosmicFavorite cosmic) {
                JsonElement el = arr.get(i);
                if (el.isJsonObject()) {
                    el.getAsJsonObject().addProperty("cosmicAmount", cosmic.getAmount());
                }
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private static void cosmiccore$loadAmounts(JsonArray arr, CallbackInfo ci) {
        if (arr == null) return;

        for (int i = 0; i < arr.size() && i < favorites.size(); i++) {
            JsonElement el = arr.get(i);
            if (!el.isJsonObject()) continue;

            JsonObject obj = el.getAsJsonObject();
            if (!obj.has("cosmicAmount")) continue;

            long amount = obj.get("cosmicAmount").getAsLong();
            favorites.set(i, new CosmicFavorite(favorites.get(i).getStack(), amount));
        }
    }
}
