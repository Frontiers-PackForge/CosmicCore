package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.integration.jade.GTJadePlugin;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.harvest.SimpleToolHandler;

import java.util.List;
import java.util.Objects;

@Mixin(value = GTJadePlugin.class, remap = false)
public abstract class GTJadeHarvestToolFixMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void cosmiccore$registerMaterialHarvestTools(CallbackInfo ci) {
        GTMaterialItems.TOOL_ITEMS.columnMap().forEach((type, map) -> {
            if (type.harvestTags.isEmpty() ||
                    type.harvestTags.getFirst().location().getNamespace().equals("minecraft") || map.isEmpty()) {
                return;
            }

            List<Item> tools = map.values().stream()
                    .filter(Objects::nonNull)
                    .filter(ItemProviderEntry::isBound)
                    .map(ItemProviderEntry::asItem)
                    .toList();
            if (!tools.isEmpty()) {
                HarvestToolProvider.registerHandler(SimpleToolHandler.create(GTCEu.id(type.name), tools, true));
            }
        });
    }
}
