package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.FluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaStorage;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.server.ServerScriptManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Reads KubeJS recipe schemas so the Recipe Forge can auto-generate an editor for any recipe type that has one
 * (vanilla, GregTech, or any mod that ships/derives a KubeJS schema), without hand-writing a layout per type. The
 * live {@link RecipeSchemaStorage} only exists server-side (on {@link ServerScriptManager}), reached via the
 * recipe manager- which is why schema-driven editors must be built in the picker's server-side click handler,
 * where the resulting widgets then sync to the client like the GregTech slot rebuild does.
 */
public final class RecipeSchemaReader {

    public enum Kind { ITEM, FLUID, NUMBER, BOOLEAN, STRING, OTHER }
    public record SchemaField(String name, ComponentRole role, Kind kind, boolean list, RecipeKey<?> key) {}

    private RecipeSchemaReader() {}

    public static RecipeSchemaStorage storage(Player player) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;
        if (server.getRecipeManager() instanceof RecipeManagerKJS kjs && kjs.kjs$getResources() != null) {
            ServerScriptManager manager = kjs.kjs$getResources().kjs$getServerScriptManager();
            return manager == null ? null : manager.recipeSchemaStorage;
        }
        return null;
    }

    public static List<String> typeIds(Player player) {
        List<String> ids = new ArrayList<>();
        RecipeSchemaStorage storage = storage(player);
        if (storage == null) return ids;
        for (Map.Entry<String, RecipeNamespace> entry : storage.namespaces.entrySet()) {
            for (String path : entry.getValue().keySet()) {
                ids.add(entry.getKey() + ":" + path);
            }
        }
        ids.sort(Comparator.naturalOrder());
        return ids;
    }

    public static List<SchemaField> fields(Player player, String typeId) {
        List<SchemaField> fields = new ArrayList<>();
        RecipeSchemaStorage storage = storage(player);
        if (storage == null) return fields;
        int colon = typeId.indexOf(':');
        if (colon < 0) return fields;
        RecipeNamespace namespace = storage.namespaces.get(typeId.substring(0, colon));
        if (namespace == null) return fields;
        RecipeSchemaType type = namespace.get(typeId.substring(colon + 1));
        if (type == null) return fields;
        for (RecipeKey<?> key : type.schema.keys) {
            boolean list = key.component instanceof ListRecipeComponent;
            RecipeComponent<?> base = list ? ((ListRecipeComponent<?>) key.component).component() : key.component;
            fields.add(new SchemaField(key.name, key.role, kindOf(base), list, key));
        }
        return fields;
    }

    private static Kind kindOf(RecipeComponent<?> component) {
        if (component instanceof ItemStackComponent || component instanceof IngredientComponent
                || component instanceof SizedIngredientComponent) {
            return Kind.ITEM;
        }
        if (component instanceof FluidStackComponent || component instanceof FluidIngredientComponent
                || component instanceof SizedFluidIngredientComponent) {
            return Kind.FLUID;
        }
        if (component instanceof NumberComponent || component instanceof TimeComponent) return Kind.NUMBER;
        if (component instanceof BooleanComponent) return Kind.BOOLEAN;
        if (component instanceof StringComponent) return Kind.STRING;
        return Kind.OTHER;
    }
}
