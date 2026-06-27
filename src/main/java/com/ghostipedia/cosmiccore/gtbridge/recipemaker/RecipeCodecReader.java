package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reads a recipe type's structure from its serializer CODEC by encoding a real example recipe to JSON, then
 * inferring an editable field model from the JSON shape. Unlike {@link RecipeSchemaReader} (KubeJS schemas, which
 * are server-only), the recipe manager + serializers exist on BOTH sides, so this works identically client and
 * server - the held-item editor can be built on each side without syncing. The chosen sample is deterministic
 * (lowest recipe id) so both sides infer the same model. This is the generic engine for vanilla + any mod's
 * recipe types, and the basis for the raw-JSON export ({@code event.custom}) + load-as-template.
 */
public final class RecipeCodecReader {

    public enum Kind {
        ITEM,
        FLUID,
        NUMBER,
        STRING,
        BOOLEAN,
        LIST,
        OBJECT,
        OTHER
    }

    public record JsonField(String name, Kind kind, Kind elementKind, boolean output, int listMax) {}

    private static final int MAX_SAMPLES = 64;

    private RecipeCodecReader() {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List<RecipeHolder<?>> recipesOf(Player player, ResourceLocation typeId) {
        RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.get(typeId);
        if (type == null) return List.of();
        Collection<RecipeHolder<?>> all = (Collection) player.level().getRecipeManager()
                .getAllRecipesFor((RecipeType) type);
        List<RecipeHolder<?>> sorted = new ArrayList<>(all);
        sorted.sort(Comparator.comparing(holder -> holder.id().toString()));
        return sorted;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static JsonObject encode(RecipeHolder<?> holder, RegistryOps<JsonElement> ops) {
        Recipe<?> recipe = holder.value();
        Codec codec = recipe.getSerializer().codec().codec();
        Object encoded = codec.encodeStart(ops, recipe).result().orElse(null);
        return encoded instanceof JsonObject json ? json : null;
    }

    /** The lowest-id recipe of the type, encoded to JSON - used as the editable template (deterministic). */
    public static JsonObject sample(Player player, ResourceLocation typeId) {
        List<RecipeHolder<?>> recipes = recipesOf(player, typeId);
        if (recipes.isEmpty()) return null;
        return encode(recipes.getFirst(), RegistryOps.create(JsonOps.INSTANCE, player.level().registryAccess()));
    }

    public static List<JsonField> fields(Player player, ResourceLocation typeId) {
        List<JsonField> fields = new ArrayList<>();
        List<RecipeHolder<?>> recipes = recipesOf(player, typeId);
        if (recipes.isEmpty()) return fields;
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, player.level().registryAccess());
        JsonObject base = encode(recipes.getFirst(), ops);
        if (base == null) return fields;

        Map<String, Integer> listMax = new HashMap<>();
        int limit = Math.min(recipes.size(), MAX_SAMPLES);
        for (int i = 0; i < limit; i++) {
            JsonObject json = encode(recipes.get(i), ops);
            if (json == null) continue;
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (entry.getValue().isJsonArray()) {
                    listMax.merge(entry.getKey(), entry.getValue().getAsJsonArray().size(), Math::max);
                }
            }
        }

        for (Map.Entry<String, JsonElement> entry : base.entrySet()) {
            if (entry.getKey().equals("type")) continue;
            JsonElement value = entry.getValue();
            Kind kind = kindOf(value);
            Kind element = Kind.OTHER;
            if (kind == Kind.LIST && !value.getAsJsonArray().isEmpty()) {
                element = kindOf(value.getAsJsonArray().get(0));
            }
            fields.add(new JsonField(entry.getKey(), kind, element, isOutput(entry.getKey()),
                    listMax.getOrDefault(entry.getKey(), 0)));
        }
        return fields;
    }

    private static Kind kindOf(JsonElement element) {
        if (element.isJsonArray()) return Kind.LIST;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("fluid")) return Kind.FLUID;
            if (object.has("item") || object.has("tag") || object.has("id")) return Kind.ITEM;
            return Kind.OBJECT;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) return Kind.NUMBER;
            if (primitive.isBoolean()) return Kind.BOOLEAN;
            return Kind.STRING;
        }
        return Kind.OTHER;
    }

    private static boolean isOutput(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        return lower.contains("result") || lower.contains("output");
    }
}
