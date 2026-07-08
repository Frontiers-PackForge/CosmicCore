package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.abyss.AbyssBudget;
import com.ghostipedia.cosmiccore.common.airControl.OxygenBudget;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodData;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionData;
import com.ghostipedia.cosmiccore.common.teleporter.TeleportOrigin;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.mojang.serialization.Codec;

public class CosmicAttachmentTypes {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, CosmicCore.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReflectionData>> REFLECTION = ATTACHMENT_TYPES
            .register("reflection",
                    () -> AttachmentType.serializable(holder -> new ReflectionData()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OxygenBudget>> OXYGEN_BUDGET = ATTACHMENT_TYPES
            .register("oxygen_budget",
                    () -> AttachmentType.serializable(holder -> new OxygenBudget()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AbyssBudget>> ABYSS_BUDGET = ATTACHMENT_TYPES
            .register("abyss_budget",
                    () -> AttachmentType.serializable(holder -> new AbyssBudget()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TeleportOrigin>> TELEPORT_ORIGIN = ATTACHMENT_TYPES
            .register("teleport_origin",
                    () -> AttachmentType.serializable(holder -> new TeleportOrigin()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CosmicFoodData>> FOOD_DATA = ATTACHMENT_TYPES
            .register("food_data",
                    () -> AttachmentType.serializable(holder -> new CosmicFoodData()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> ABYSS_ATTUNED = ATTACHMENT_TYPES
            .register("abyss_attuned",
                    () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build());
}
