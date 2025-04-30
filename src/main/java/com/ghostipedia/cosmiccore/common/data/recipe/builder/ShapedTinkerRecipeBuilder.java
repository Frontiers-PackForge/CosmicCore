package com.ghostipedia.cosmiccore.common.data.recipe.builder;

import com.ghostipedia.cosmiccore.api.recipe.ShapedTinkerRecipe;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.data.recipe.builder.ShapedRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ShapedTinkerRecipeBuilder extends ShapedRecipeBuilder {

    public ShapedTinkerRecipeBuilder(@Nullable ResourceLocation id) {
        super(id);
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject jsonObject) {
                toJson(jsonObject);
            }

            @Override
            public ResourceLocation getId() {
                var ID = id == null ? defaultId() : id;
                return new ResourceLocation(ID.getNamespace(), "shaped_tinker/" + ID.getPath());
            }

            @Override
            public RecipeSerializer<?> getType() {
                return ShapedTinkerRecipe.SERIALIZER;
            }

            @Override
            public @Nullable JsonObject serializeAdvancement() {
                return null;
            }

            @Override
            public @Nullable ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}
