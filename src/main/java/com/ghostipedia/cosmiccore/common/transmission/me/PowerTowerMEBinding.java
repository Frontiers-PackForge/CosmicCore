package com.ghostipedia.cosmiccore.common.transmission.me;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public record PowerTowerMEBinding(UUID circuit, ResourceLocation dimension, BlockPos input) {

    private static final String CARD_KEY = "cosmiccore:power_tower_circuit";

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putUUID("Circuit", circuit);
        tag.putString("Dimension", dimension.toString());
        tag.putLong("Input", input.asLong());
        return tag;
    }

    public static @Nullable PowerTowerMEBinding load(CompoundTag tag) {
        var dimension = ResourceLocation.tryParse(tag.getString("Dimension"));
        if (!tag.hasUUID("Circuit") || dimension == null || !tag.contains("Input")) return null;
        return new PowerTowerMEBinding(tag.getUUID("Circuit"), dimension, BlockPos.of(tag.getLong("Input")));
    }

    public Map<String, String> cardSettings() {
        return Map.of(CARD_KEY, circuit.toString(), "dimension", dimension.toString(), "input",
                Long.toString(input.asLong()));
    }

    public static @Nullable PowerTowerMEBinding fromCard(Map<String, String> settings) {
        try {
            var dimension = ResourceLocation.tryParse(settings.getOrDefault("dimension", ""));
            if (dimension == null || !settings.containsKey(CARD_KEY)) return null;
            return new PowerTowerMEBinding(UUID.fromString(settings.get(CARD_KEY)), dimension,
                    BlockPos.of(Long.parseLong(settings.getOrDefault("input", ""))));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
