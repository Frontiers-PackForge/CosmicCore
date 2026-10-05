package com.ghostipedia.cosmiccore.common.compat.gtceu;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;

public final class EnergyTransferRecipeIngredients {

    private EnergyTransferRecipeIngredients() {}

    public static Ingredient of(boolean strict, ItemStack template, Ingredient chargeIngredient,
                                DataComponentType<?> energyComponent) {
        if (template.has(energyComponent) && chargeIngredient.test(template) &&
                template.getComponentsPatch().entrySet().stream()
                        .allMatch(entry -> entry.getKey() == energyComponent)) {
            return IntersectionIngredient.of(Ingredient.of(template.getItem()), chargeIngredient);
        }
        return DataComponentIngredient.of(strict, template);
    }
}
