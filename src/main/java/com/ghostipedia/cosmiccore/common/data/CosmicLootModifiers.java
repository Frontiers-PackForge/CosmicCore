package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.glm.GenericLootModifier;
import com.ghostipedia.cosmiccore.common.glm.NoSilkTouchOreLootModifier;

import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.mojang.serialization.Codec;

public class CosmicLootModifiers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS = DeferredRegister
            .create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CosmicCore.MOD_ID);

    public static final RegistryObject<Codec<GenericLootModifier>> GENERIC_LOOT_MODIFIER = GLOBAL_LOOT_MODIFIERS
            .register("generic", () -> GenericLootModifier.CODEC);

    public static final RegistryObject<Codec<NoSilkTouchOreLootModifier>> NO_SILK_TOUCH_ORE = GLOBAL_LOOT_MODIFIERS
            .register("no_silk_touch_ore", () -> NoSilkTouchOreLootModifier.CODEC);

    public static void register(IEventBus bus) {
        GLOBAL_LOOT_MODIFIERS.register(bus);
    }
}
