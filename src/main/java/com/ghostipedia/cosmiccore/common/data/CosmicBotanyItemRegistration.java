package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTags;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.world.item.ItemStack;

import com.tterrag.registrate.util.entry.ItemEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicBotanyItemRegistration {

    public static final ItemEntry<ComponentItem> DRIFTWEED = REGISTRATE.item("driftweed", ComponentItem::new)
            .lang("Driftweed")
            .properties(p -> p.stacksTo(16))
            .tag(CosmicItemTags.AEROPONICS_SEEDLINGS)
            .defaultModel()
            .register();

    public enum CosmicBotanyItem {

        DRIFTWEED(CosmicBotanyItemRegistration.DRIFTWEED.asStack());

        public ItemStack item;

        private CosmicBotanyItem(ItemStack item) {
            this.item = item;
        }
    };

    public static void init() {}
}
