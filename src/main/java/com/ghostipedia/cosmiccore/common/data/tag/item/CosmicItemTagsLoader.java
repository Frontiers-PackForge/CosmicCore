package com.ghostipedia.cosmiccore.common.data.tag.item;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CosmicItemTagsLoader {

    public static void init(RegistrateTagsProvider<Item> provider) {
        create(provider, CosmicItemTags.NANOMUSCLE_SPACE_SUITE, GTItems.NANO_HELMET, GTItems.NANO_LEGGINGS, GTItems.NANO_BOOTS);
        create(provider, CosmicItemTags.QUARKTECH_SPACE_SUITE, GTItems.QUANTUM_HELMET, GTItems.QUANTUM_LEGGINGS, GTItems.QUANTUM_BOOTS);
    }

    private static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, ItemEntry<?>... items) {
        var builder = provider.addTag(tagKey);
        for (ItemEntry<?> itemEntry : items) builder.add(TagEntry.element(itemEntry.getId()));
    }

}
