package com.ghostipedia.cosmiccore.common.data.tag.item;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.ItemEntry;

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
        create(provider, ItemTags.FREEZE_IMMUNE_WEARABLES, travelerBoots());
        create(provider, ItemTags.FOOT_ARMOR, travelerBoots());
        create(provider, ItemTags.FOOT_ARMOR_ENCHANTABLE, travelerBoots());
        create(provider, ItemTags.TRIMMABLE_ARMOR, travelerBoots());
        create(provider, CosmicItemTags.PRESSURE_RATED_1,
                optional("create", "netherite_diving_helmet"),
                optional("create", "netherite_backtank"),
                optional("create", "netherite_diving_boots"),
                required("minecraft", "netherite_leggings"),
                required("minecraft", "netherite_boots"));
        create(provider, CosmicItemTags.PRESSURE_RATED_2,
                CosmicItems.SHADEBLOOM_DIVING_HELMET,
                CosmicItems.SHADEBLOOM_CHESTPLATE,
                CosmicItems.SHADEBLOOM_LEGGINGS,
                CosmicItems.SHADEBLOOM_BOOTS,
                CosmicItems.SHADEBLOOM_DIVING_BOOTS);
        create(provider, CosmicItemTags.PRESSURE_RATED_3,
                GTItems.NANO_HELMET,
                GTItems.NANO_CHESTPLATE,
                GTItems.NANO_CHESTPLATE_ADVANCED,
                GTItems.NANO_LEGGINGS,
                GTItems.NANO_BOOTS);
        provider.addTag(CosmicItemTags.PRESSURE_RATED_4);
        // TODO(stellaris): re-add AA ModItemTags (SPACE_SUITS / FREEZE_RESISTANT_ARMOR / HEAT_RESISTANT_ARMOR)
        // post-Ad-Astra
    }

    private static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, ItemEntry<?>... items) {
        var builder = provider.addTag(tagKey);
        for (ItemEntry<?> itemEntry : items) builder.add(TagEntry.element(itemEntry.getId()));
    }

    private static void create(RegistrateTagsProvider<Item> provider, TagKey<Item> tagKey, TagEntry... items) {
        var builder = provider.addTag(tagKey);
        for (TagEntry item : items) builder.add(item);
    }

    private static TagEntry required(String namespace, String path) {
        return TagEntry.element(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TagEntry optional(String namespace, String path) {
        return TagEntry.optionalElement(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static ItemEntry<?>[] travelerBoots() {
        return new ItemEntry<?>[] {
                CosmicItems.STEEL_TRAVELERS_BOOTS,
                CosmicItems.NETHERITE_TRAVELERS_BOOTS,
                CosmicItems.SHADEBLOOM_BOOTS,
                CosmicItems.NANO_BOOTS,
                CosmicItems.QUARK_BOOTS
        };
    }
}
