package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Set;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class CrushedOreTooltipHandler {

    private CrushedOreTooltipHandler() {}

    private static final Set<String> DISABLED_CAULDRON_KEYS = Set.of(
            "metaitem.crushed.tooltip.purify",
            "metaitem.dust.tooltip.purify");

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof TagPrefixItem item)) return;

        event.getToolTip().removeIf(line -> line.getContents() instanceof TranslatableContents contents &&
                DISABLED_CAULDRON_KEYS.contains(contents.getKey()));

        if (item.tagPrefix != TagPrefix.crushed) return;
        MaterialStack output = CosmicBundleMaterials.handSortOutput(item.material);
        if (output == null) return;
        event.getToolTip().add(Component
                .translatable("cosmiccore.tooltip.hand_sort", output.material().getLocalizedName())
                .withStyle(ChatFormatting.GRAY));
    }
}
