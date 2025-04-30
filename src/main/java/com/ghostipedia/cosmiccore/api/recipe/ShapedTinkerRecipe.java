package com.ghostipedia.cosmiccore.api.recipe;

import com.ghostipedia.cosmiccore.api.recipe.ingredient.TinkerIngredient;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.ShapedEnergyTransferRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.core.mixins.ShapedRecipeAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ShapedTinkerRecipe extends ShapedRecipe {

    public static final RecipeSerializer<ShapedTinkerRecipe> SERIALIZER = new Serializer();

    public ShapedTinkerRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> recipeItems, ItemStack result, boolean showNotification) {
        super(id, group, category, width, height, recipeItems, result, showNotification);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> items = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        // figure out all the fluid container ingredients' remainders.
        int replacedSlot = -1;
        OUTER_LOOP:
        for (int x = 0; x <= inv.getWidth() - this.getWidth(); ++x) {
            for (int y = 0; y <= inv.getHeight() - this.getHeight(); ++y) {
                var stack = this.findToolReplacement(inv, x, y, false);
                if (stack.getFirst() != -1) {
                    items.set(stack.getFirst(), stack.getSecond());
                    replacedSlot = stack.getFirst();
                    break OUTER_LOOP;
                }

                stack = this.findToolReplacement(inv, x, y, true);
                if (stack.getFirst() != -1) {
                    items.set(stack.getFirst(), stack.getSecond());
                    replacedSlot = stack.getFirst();
                    break OUTER_LOOP;
                }
            }
        }

        for (int i = 0; i < items.size(); ++i) {
            if (i == replacedSlot) {
                continue;
            }
            ItemStack item = inv.getItem(i);
            if (item.hasCraftingRemainingItem()) {
                items.set(i, item.getCraftingRemainingItem());
            }
        }

        return items;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private Pair<Integer, ItemStack> findToolReplacement(CraftingContainer inv, int width, int height,
                                                          boolean mirrored) {
        for (int x = 0; x < inv.getWidth(); ++x) {
            for (int y = 0; y < inv.getHeight(); ++y) {
                int offsetX = x - width;
                int offsetY = y - height;
                Ingredient ingredient = Ingredient.EMPTY;
                if (offsetX >= 0 && offsetY >= 0 && offsetX < this.getWidth() && offsetY < this.getHeight()) {
                    if (mirrored) {
                        ingredient = this.getIngredients()
                                .get(this.getWidth() - offsetX - 1 + offsetY * this.getWidth());
                    } else {
                        ingredient = this.getIngredients().get(offsetX + offsetY * this.getWidth());
                    }
                }

                if (ingredient instanceof TinkerIngredient tinkerTool) {
                    int slot = x + y * inv.getWidth();
                    ItemStack stack = inv.getItem(slot);
                    if (tinkerTool.test(stack)) {
                        return Pair.of(slot, stack);
                    }
                }
            }
        }

        return Pair.of(-1, ItemStack.EMPTY);
    }

    public static class Serializer implements RecipeSerializer<ShapedTinkerRecipe> {
        @Override
        public ShapedTinkerRecipe fromJson(ResourceLocation resourceLocation, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC
                    .byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
            Map<String, Ingredient> key = ShapedRecipeAccessor.callKeyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            String[] pattern = ShapedRecipeAccessor
                    .callShrink(ShapedRecipeAccessor.callPatternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
            int xSize = pattern[0].length();
            int ySize = pattern.length;
            NonNullList<Ingredient> dissolved = ShapedRecipeAccessor.callDissolvePattern(pattern, key, xSize, ySize);
            ItemStack result = ShapedEnergyTransferRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);
            return new ShapedTinkerRecipe(resourceLocation, group, category, xSize, ySize, dissolved, result, showNotification);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedTinkerRecipe recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());
            buffer.writeEnum(recipe.category());
            buffer.writeUtf(recipe.getGroup());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(((ShapedRecipeAccessor) recipe).getResult());
            buffer.writeBoolean(recipe.showNotification());
        }

        @Override
        public @Nullable ShapedTinkerRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf buffer) {
            int xSize = buffer.readVarInt();
            int ySize = buffer.readVarInt();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            String group = buffer.readUtf();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(xSize * ySize, Ingredient.EMPTY);
            ingredients.replaceAll($ -> Ingredient.fromNetwork(buffer));
            ItemStack result = buffer.readItem();
            boolean showNotification = buffer.readBoolean();
            return new ShapedTinkerRecipe(resourceLocation, group, category, xSize, ySize, ingredients, result, showNotification);
        }
    }
}
