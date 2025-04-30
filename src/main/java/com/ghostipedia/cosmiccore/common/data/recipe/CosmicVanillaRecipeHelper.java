package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.common.data.recipe.builder.ShapedTinkerRecipeBuilder;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper.getRecyclingIngredients;

public class CosmicVanillaRecipeHelper {
    private static void addShapedTinkerRecipe(Consumer<FinishedRecipe> provider, boolean setMaterialInfoData,
                                              boolean isStrict, @NotNull ResourceLocation regName, @NotNull ItemStack result,
                                              @NotNull Object... recipe) {
        var builder = new ShapedTinkerRecipeBuilder(regName).output(result);
        builder.isStrict(isStrict);
        final CharSet tools = ToolHelper.getToolSymbols();
        CharSet foundTools = new CharArraySet(9);
        for (int i = 0; i < recipe.length; i++) {
            var o = recipe[i];
            if (o instanceof String pattern) {
                builder.pattern(pattern);
                for (char c : pattern.toCharArray()) {
                    if (tools.contains(c)) {
                        foundTools.add(c);
                    }
                }
            }
            if (o instanceof String[] pattern) {
                for (String s : pattern) {
                    builder.pattern(s);
                    for (char c : s.toCharArray()) {
                        if (tools.contains(c)) {
                            foundTools.add(c);
                        }
                    }
                }
            }
            if (o instanceof Character sign) {
                var content = recipe[i + 1];
                i++;
                if (content instanceof Ingredient ingredient) {
                    builder.define(sign, ingredient);
                } else if (content instanceof ItemStack itemStack) {
                    builder.define(sign, itemStack);
                } else if (content instanceof TagKey<?> key) {
                    builder.define(sign, (TagKey<Item>) key);
                } else if (content instanceof TagPrefix prefix) {
                    if (prefix.getItemParentTags().length > 0) {
                        builder.define(sign, prefix.getItemParentTags()[0]);
                    }
                } else if (content instanceof ItemLike itemLike) {
                    builder.define(sign, itemLike);
                } else if (content instanceof MaterialEntry entry) {
                    TagKey<Item> tag = ChemicalHelper.getTag(entry.tagPrefix(), entry.material());
                    if (tag != null) {
                        builder.define(sign, tag);
                    } else builder.define(sign, ChemicalHelper.get(entry.tagPrefix(), entry.material()));
                } else if (content instanceof ItemProviderEntry<?> entry) {
                    builder.define(sign, entry.asStack());
                }
            }
        }
        for (var it = foundTools.iterator(); it.hasNext();) {
            char c = it.nextChar();
            builder.define(c, ToolHelper.getToolFromSymbol(c).itemTags.get(0));
        }

        builder.save(provider);

        if (setMaterialInfoData) {
            ItemMaterialData.registerMaterialInfo(result.getItem(), getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    public void addShapedTinkerRecipe(Consumer<FinishedRecipe> provider, boolean setMaterialInfoData,
                                      @NotNull String regName, @NotNull ItemStack result,
                                      @NotNull Object... recipe) {
        addShapedTinkerRecipe(provider, setMaterialInfoData, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedTinkerRecipe(Consumer<FinishedRecipe> provider, boolean setMaterialInfoData,
                                             @NotNull ResourceLocation regName, @NotNull ItemStack result,

                                             @NotNull Object... recipe) {
        addShapedTinkerRecipe(provider, setMaterialInfoData, false, regName, result, recipe);
    }

    public static void addShapedTinkerRecipe(Consumer<FinishedRecipe> provider, @NotNull String regName,
                                             @NotNull ItemStack result,
                                             @NotNull Object... recipe) {
        addShapedTinkerRecipe(provider, GTCEu.id(regName), result, recipe);
    }

    public static void addShapedTinkerRecipe(Consumer<FinishedRecipe> provider,
                                             @NotNull ResourceLocation regName,
                                             @NotNull ItemStack result,
                                             @NotNull Object... recipe) {
        addShapedTinkerRecipe(provider, false, regName, result, recipe);
    }
}
