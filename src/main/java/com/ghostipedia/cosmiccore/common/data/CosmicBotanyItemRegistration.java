package com.ghostipedia.cosmiccore.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.world.item.ItemStack;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicBotanyItemRegistration {

    public static final ItemEntry<ComponentItem> DRIFTWEED = REGISTRATE.item("driftweed", ComponentItem::new)
            .lang("Driftweed")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> SOUL_GOURD = REGISTRATE
            .item("soul_gourd", ComponentItem::new)
            .lang("Soul Gourd")
            .properties(p -> p.stacksTo(16))
            .tag()
            .model(NonNullBiConsumer.noop())
            .register();

    public enum CosmicBotanyItem {

        DRIFTWEED(CosmicBotanyItemRegistration.DRIFTWEED.asStack()),

        SOUL_GOURD(CosmicBotanyItemRegistration.SOUL_GOURD.asStack());

        public ItemStack item;

        private CosmicBotanyItem(ItemStack item) {
            this.item = item;
        }
    };

    public static void init() {}
}
