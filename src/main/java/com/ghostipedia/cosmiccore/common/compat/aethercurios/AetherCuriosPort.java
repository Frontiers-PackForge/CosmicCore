package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class AetherCuriosPort {

    private static final ResourceLocation SOUND_GENERIC = aether("item.accessory.equip_generic");
    private static final ResourceLocation SOUND_CAPE = aether("item.accessory.equip_cape");

    private AetherCuriosPort() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("aether")) return;
        event.enqueueWork(() -> {
            register();
        });
    }

    private static void register() {
        glove("leather_gloves", 0.25, mc("item.armor.equip_leather"));
        glove("chainmail_gloves", 0.35, mc("item.armor.equip_chain"));
        glove("iron_gloves", 0.5, mc("item.armor.equip_iron"));
        glove("golden_gloves", 0.25, mc("item.armor.equip_gold"));
        glove("diamond_gloves", 0.75, mc("item.armor.equip_diamond"));
        glove("netherite_gloves", 1.0, mc("item.armor.equip_netherite"));
        register("zanite_gloves", new AetherZaniteGloveCurio(aether("item.armor.equip_zanite")));
        glove("gravitite_gloves", 0.75, aether("item.armor.equip_gravitite"));
        glove("valkyrie_gloves", 1.0, aether("item.armor.equip_valkyrie"));
        glove("neptune_gloves", 0.5, aether("item.armor.equip_neptune"));
        glove("phoenix_gloves", 1.0, aether("item.armor.equip_phoenix"));
        glove("obsidian_gloves", 1.0, aether("item.armor.equip_obsidian"));

        plain("iron_ring", aether("item.accessory.equip_iron_ring"));
        plain("golden_ring", aether("item.accessory.equip_gold_ring"));
        plain("zanite_ring", aether("item.accessory.equip_zanite_ring"));
        register("ice_ring", new AetherFreezingCurio(aether("item.accessory.equip_ice_ring")));

        plain("iron_pendant", aether("item.accessory.equip_iron_pendant"));
        plain("golden_pendant", aether("item.accessory.equip_gold_pendant"));
        plain("zanite_pendant", aether("item.accessory.equip_zanite_pendant"));
        register("ice_pendant", new AetherFreezingCurio(aether("item.accessory.equip_ice_pendant")));

        register("golden_feather", new AetherSlowFallCurio(SOUND_GENERIC));
        register("regeneration_stone", new AetherRegenStoneCurio(SOUND_GENERIC));
        register("iron_bubble", new AetherIronBubbleCurio(SOUND_GENERIC));
        plain("shield_of_repulsion", SOUND_GENERIC);

        plain("red_cape", SOUND_CAPE);
        plain("blue_cape", SOUND_CAPE);
        plain("yellow_cape", SOUND_CAPE);
        plain("white_cape", SOUND_CAPE);
        plain("swet_cape", SOUND_CAPE);
        register("agility_cape", new AetherAgilityCapeCurio(SOUND_CAPE));
        register("valkyrie_cape", new AetherSlowFallCurio(SOUND_CAPE));
        plain("invisibility_cloak", SOUND_CAPE);
    }

    private static void glove(String name, double punchDamage, ResourceLocation sound) {
        register(name, new AetherGloveCurio(punchDamage, sound));
    }

    private static void plain(String name, ResourceLocation sound) {
        register(name, new AetherAccessoryCurio(sound));
    }

    private static void register(String name, ICurioItem curio) {
        BuiltInRegistries.ITEM.getOptional(aether(name))
                .ifPresent(item -> CuriosApi.registerCurio(item, curio));
    }

    private static ResourceLocation aether(String path) {
        return ResourceLocation.fromNamespaceAndPath("aether", path);
    }

    private static ResourceLocation mc(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }
}
