package com.ghostipedia.cosmiccore.common.data.tag.item;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import earth.terrarium.adastra.common.tags.ModItemTags;

import java.util.stream.Stream;

public class CosmicItemTagsLoader {

    public static ItemEntry<?>[] NANO_SPACE_SUITE = { GTItems.NANO_HELMET, GTItems.NANO_LEGGINGS, GTItems.NANO_BOOTS };
    public static ItemEntry<?>[] QUANTUM_SPACE_SUITE = { GTItems.QUANTUM_HELMET, GTItems.QUANTUM_LEGGINGS,
            GTItems.QUANTUM_BOOTS };
    public static ItemEntry<?>[] SPACE_SUITES = Stream.of(NANO_SPACE_SUITE, QUANTUM_SPACE_SUITE).flatMap(Stream::of)
            .toArray(ItemEntry<?>[]::new);

    public static void init(RegistrateTagsProvider<Item> provider) {
        create(provider, CosmicItemTags.NANOMUSCLE_SPACE_SUITE, NANO_SPACE_SUITE);
        create(provider, CosmicItemTags.QUARKTECH_SPACE_SUITE, QUANTUM_SPACE_SUITE);

        create(provider, ModItemTags.SPACE_SUITS, SPACE_SUITES);
        create(provider, ModItemTags.FREEZE_RESISTANT_ARMOR, SPACE_SUITES);
        create(provider, ModItemTags.HEAT_RESISTANT_ARMOR, SPACE_SUITES);
    }

    private static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, ItemEntry<?>... items) {
        var builder = provider.addTag(tagKey);
        for (ItemEntry<?> itemEntry : items) builder.add(TagEntry.element(itemEntry.getId()));
    }
}
