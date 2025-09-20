package com.ghostipedia.cosmiccore.api.plugins;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;

import java.util.ArrayList;
import java.util.List;

@EmiEntrypoint
public class CosmicEMIPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        // Show all material variants for every TCon tool part
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.TOOL_PARTS)) {
            Item item = holder.value();
            if (item instanceof IMaterialItem materialItem) {
                List<ItemStack> variants = new ArrayList<>();
                materialItem.addVariants(variants::add, "");
                for (ItemStack stack : variants) {
                    registry.addEmiStack(EmiStack.of(stack));
                }
            }
        }
    }
}