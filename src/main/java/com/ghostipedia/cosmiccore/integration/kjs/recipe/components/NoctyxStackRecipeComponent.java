package com.ghostipedia.cosmiccore.integration.kjs.recipe.components;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;

public class NoctyxStackRecipeComponent implements RecipeComponent<NoctyxStack> {

    public static final NoctyxStackRecipeComponent INSTANCE = new NoctyxStackRecipeComponent();

    private static NoctyxStack stackOf(Object from) {
        if (from instanceof NoctyxStack stack) {
            return stack;
        } else if (from instanceof JsonObject jsonObject) {
            return NoctyxStack.SERIALIZER.fromJson(jsonObject);
        }
        throw new IllegalStateException("Expected a stack of noctyx!");
    }

    @Override
    public Class<?> componentClass() {
        return NoctyxStack.class;
    }

    @Override
    public JsonElement write(RecipeJS recipe, NoctyxStack value) {
        return NoctyxStack.SERIALIZER.toJson(value);
    }

    @Override
    public NoctyxStack read(RecipeJS recipe, Object from) {
        return stackOf(from);
    }

    @Override
    public String componentType() {
        return "noctyxStack";
    }
}
