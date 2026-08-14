package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.gravity.GravityFrame;
import com.ghostipedia.cosmiccore.common.abyss.AbyssBudget;
import com.ghostipedia.cosmiccore.common.airControl.OxygenBudget;
import com.ghostipedia.cosmiccore.common.firmament.FirmamentTraversalState;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodData;
import com.ghostipedia.cosmiccore.common.gravity.GravityRuntimeState;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.mojang.serialization.Codec;

public class CosmicAttachmentTypes {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, CosmicCore.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OxygenBudget>> OXYGEN_BUDGET = ATTACHMENT_TYPES
            .register("oxygen_budget",
                    () -> AttachmentType.serializable(holder -> new OxygenBudget()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AbyssBudget>> ABYSS_BUDGET = ATTACHMENT_TYPES
            .register("abyss_budget",
                    () -> AttachmentType.serializable(holder -> new AbyssBudget()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CosmicFoodData>> FOOD_DATA = ATTACHMENT_TYPES
            .register("food_data",
                    () -> AttachmentType.serializable(holder -> new CosmicFoodData()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> ABYSS_ATTUNED = ATTACHMENT_TYPES
            .register("abyss_attuned",
                    () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GravityFrame>> GRAVITY_FRAME = ATTACHMENT_TYPES
            .register("gravity_frame",
                    () -> AttachmentType.builder(GravityFrame::normal).sync(GravityFrame.STREAM_CODEC).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GravityRuntimeState>> GRAVITY_RUNTIME = ATTACHMENT_TYPES
            .register("gravity_runtime", () -> AttachmentType.builder(GravityRuntimeState::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FirmamentTraversalState>> FIRMAMENT_TRAVERSAL_STATE = ATTACHMENT_TYPES
            .register("firmament_traversal_state",
                    () -> AttachmentType.builder(() -> FirmamentTraversalState.INACTIVE)
                            .sync(FirmamentTraversalState.STREAM_CODEC)
                            .build());
}
