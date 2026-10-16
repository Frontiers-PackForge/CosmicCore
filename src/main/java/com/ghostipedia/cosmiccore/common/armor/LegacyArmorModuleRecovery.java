package com.ghostipedia.cosmiccore.common.armor;

import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.item.module.ModularItemData;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.module.FluidStorageModule;

import net.minecraft.world.item.ItemStack;

public final class LegacyArmorModuleRecovery {

    private LegacyArmorModuleRecovery() {}

    public static ModularItemData recover(ItemStack stack, ModularItemData data) {
        var fluid = stack.get(GTDataComponents.FLUID_CONTENT);
        for (var module : data.getModules()) {
            if (!(module.getModule() instanceof FluidStorageModule)) continue;
            var item = module.getModuleItem().copy();
            if (ItemData.readTag(item).getBoolean("CosmicLegacyStorageRecovered")) return data;
            if (fluid == null || fluid.isEmpty()) item.remove(GTDataComponents.FLUID_CONTENT);
            else item.set(GTDataComponents.FLUID_CONTENT, fluid);
            ItemData.mutateTag(item, tag -> tag.putBoolean("CosmicLegacyStorageRecovered", true));
            var recovered = data.withModuleInSlot(module.getSlot(), module.withModuleItem(item));
            stack.set(GTDataComponents.MODULAR_ITEM_DATA, recovered);
            stack.remove(GTDataComponents.FLUID_CONTENT);
            return recovered;
        }
        return data;
    }
}
