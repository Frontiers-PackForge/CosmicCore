package com.ghostipedia.cosmiccore.common.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.world.item.ItemStack;

public final class BlastingRecipeCompatibility {

    private BlastingRecipeCompatibility() {}

    public static boolean permits(ItemStack output) {
        MaterialStack materialStack = ChemicalHelper.getMaterialStack(output);
        return !materialStack.isEmpty() && permits(ChemicalHelper.getPrefix(output),
                materialStack.material().getBlastTemperature());
    }

    static boolean permits(TagPrefix prefix, int blastTemperature) {
        return prefix == TagPrefix.ingot && blastTemperature == 0;
    }
}
