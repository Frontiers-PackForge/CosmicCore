package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.core.MixinHelpers;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(value = MixinHelpers.class, remap = false)
public abstract class GTToolEnchantmentTagsMixin {

    @Inject(method = "generateGTDynamicTags", at = @At("TAIL"))
    private static <T> void cosmiccore$addToolEnchantmentTags(
                                                              Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                                              Registry<T> registry, CallbackInfo ci) {
        if (registry != BuiltInRegistries.ITEM) {
            return;
        }

        GTMaterialItems.TOOL_ITEMS.values().forEach(item -> {
            if (item == null) {
                return;
            }

            var tool = item.get();
            TagLoader.EntryWithSource entry = MixinHelpers.makeItemEntry(tool);
            tagMap.computeIfAbsent(ItemTags.DURABILITY_ENCHANTABLE.location(), key -> new ArrayList<>()).add(entry);

            if (tool.getToolStats().isSuitableForBlockBreak(tool.getDefaultInstance())) {
                tagMap.computeIfAbsent(ItemTags.MINING_ENCHANTABLE.location(), key -> new ArrayList<>()).add(entry);
                tagMap.computeIfAbsent(ItemTags.MINING_LOOT_ENCHANTABLE.location(), key -> new ArrayList<>())
                        .add(entry);
            }
        });
    }
}
