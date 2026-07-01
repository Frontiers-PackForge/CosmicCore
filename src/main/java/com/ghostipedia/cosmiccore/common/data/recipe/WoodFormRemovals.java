package com.ghostipedia.cosmiccore.common.data.recipe;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Set;

public final class WoodFormRemovals {

    private WoodFormRemovals() {}

    public static volatile Set<ResourceLocation> forms = Set.of();

    public static boolean isCheapWoodFormRecipe(ResourceLocation id, JsonElement json) {
        Set<ResourceLocation> current = forms;
        if (current.isEmpty()) return false;
        if (id.getNamespace().equals("gtceu")) return false;
        if (!json.isJsonObject()) return false;
        JsonObject obj = json.getAsJsonObject();
        JsonElement type = obj.get("type");
        if (type == null || !type.isJsonPrimitive()) return false;
        String typeId = type.getAsString();
        if (!typeId.equals("minecraft:crafting_shaped") && !typeId.equals("minecraft:crafting_shapeless")) return false;
        ResourceLocation result = resultItem(obj.get("result"));
        return result != null && current.contains(result);
    }

    private static ResourceLocation resultItem(JsonElement result) {
        if (result == null) return null;
        if (result.isJsonPrimitive()) return ResourceLocation.tryParse(result.getAsString());
        if (result.isJsonObject()) {
            JsonObject obj = result.getAsJsonObject();
            JsonElement id = obj.has("id") ? obj.get("id") : obj.get("item");
            if (id != null && id.isJsonPrimitive()) return ResourceLocation.tryParse(id.getAsString());
        }
        return null;
    }
}
