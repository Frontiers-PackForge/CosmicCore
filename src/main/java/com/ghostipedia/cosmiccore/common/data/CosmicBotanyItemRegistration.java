package com.ghostipedia.cosmiccore.common.data;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.world.item.ItemStack;

import com.tterrag.registrate.util.entry.ItemEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicBotanyItemRegistration {

    public static final ItemEntry<ComponentItem> DULIA_LILY = REGISTRATE.item("dulia_lily", ComponentItem::new)
            .lang("Dulia Lily")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAYMARCHING_DANDILIFEON = REGISTRATE
            .item("raymarching_dandilifeon", ComponentItem::new)
            .lang("Dulia Lily")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public enum CosmicBotanyItem {

        DULIA(CosmicBotanyItemRegistration.DULIA_LILY.asStack()),

        DANDILIFEON(CosmicBotanyItemRegistration.RAYMARCHING_DANDILIFEON.asStack());

        public ItemStack item;

        private CosmicBotanyItem(ItemStack item) {
            this.item = item;
        }
    };

    public static void init() {}
}
