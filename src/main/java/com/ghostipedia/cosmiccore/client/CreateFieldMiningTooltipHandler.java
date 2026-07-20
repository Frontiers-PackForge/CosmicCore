package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.create.CreateOreFieldMiningRules;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import com.simibubi.create.AllBlocks;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class CreateFieldMiningTooltipHandler {

    private CreateFieldMiningTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        var item = event.getItemStack().getItem();
        if (item != AllBlocks.MECHANICAL_DRILL.asItem() && item != AllBlocks.DEPLOYER.asItem()) return;

        event.getToolTip().add(Component.translatable(
                "cosmiccore.tooltip.create_drill.field_fluid",
                CreateOreFieldMiningRules.BASE_FLUID_COST).withStyle(ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable(
                "cosmiccore.tooltip.create_drill.field_yield",
                Math.round(CreateOreFieldMiningRules.YIELD_CHANCE * 100)).withStyle(ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable(
                "cosmiccore.tooltip.create_drill.field_scaling",
                CreateOreFieldMiningRules.LINEAR_ACTOR_LIMIT).withStyle(ChatFormatting.GRAY));
    }
}
