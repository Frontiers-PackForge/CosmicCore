package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.glm.GenericLootModifier;
import com.ghostipedia.cosmiccore.common.glm.NoSilkTouchOreLootModifier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.mojang.serialization.MapCodec;

public class CosmicLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS = DeferredRegister
            .create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CosmicCore.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<GenericLootModifier>> GENERIC_LOOT_MODIFIER = GLOBAL_LOOT_MODIFIERS
            .register("generic", () -> GenericLootModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<NoSilkTouchOreLootModifier>> NO_SILK_TOUCH_ORE = GLOBAL_LOOT_MODIFIERS
            .register("no_silk_touch_ore", () -> NoSilkTouchOreLootModifier.CODEC);

    public static void register(IEventBus bus) {
        GLOBAL_LOOT_MODIFIERS.register(bus);
    }
}
