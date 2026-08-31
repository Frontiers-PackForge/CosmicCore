package com.ghostipedia.cosmiccore.common.vitae;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class CultivationProfileManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    public static final CultivationProfileManager INSTANCE = new CultivationProfileManager();

    private volatile Map<ResourceLocation, CultivationProfile> profiles = Map.of();

    private CultivationProfileManager() {
        super(GSON, "vitae_cultivation");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        var loaded = new LinkedHashMap<ResourceLocation, CultivationProfile>();
        entries.forEach((fileId, json) -> CultivationProfile.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(message -> CosmicCore.LOGGER.error(
                        "Invalid Vitae cultivation profile {}: {}", fileId, message))
                .ifPresent(profile -> {
                    if (!profile.isValid()) {
                        CosmicCore.LOGGER.error("Invalid Vitae cultivation ranges in {}", fileId);
                        return;
                    }
                    if (!BuiltInRegistries.ENTITY_TYPE.containsKey(profile.entity())) {
                        CosmicCore.LOGGER.warn("Skipping Vitae cultivation profile {} for missing entity {}",
                                fileId, profile.entity());
                        return;
                    }
                    for (var output : profile.itemOutputs()) {
                        if (!BuiltInRegistries.ITEM.containsKey(output.item())) {
                            CosmicCore.LOGGER.warn("Skipping Vitae cultivation profile {} for missing item {}",
                                    fileId, output.item());
                            return;
                        }
                    }
                    var previous = loaded.put(profile.entity(), profile);
                    if (previous != null) {
                        CosmicCore.LOGGER.warn("Vitae cultivation profile {} replaced another profile for {}",
                                fileId, profile.entity());
                    }
                }));
        profiles = Map.copyOf(loaded);
        CosmicCore.LOGGER.info("Loaded {} Vitae cultivation profiles", profiles.size());
    }

    public Optional<CultivationProfile> get(ResourceLocation entity) {
        return Optional.ofNullable(profiles.get(entity));
    }

    public Map<ResourceLocation, CultivationProfile> profiles() {
        return profiles;
    }
}
