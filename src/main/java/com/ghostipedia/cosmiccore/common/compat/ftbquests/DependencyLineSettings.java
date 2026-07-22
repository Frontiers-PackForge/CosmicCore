package com.ghostipedia.cosmiccore.common.compat.ftbquests;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record DependencyLineSettings(boolean visible, String asset) {

    public static final String MAIN_QUESTLINE_ASSET = "cosmiccore:textures/gui/ftbquests/dependency_lines/main_quest_line.png";
    public static final String OFFROAD_ASSET = "cosmiccore:textures/gui/ftbquests/dependency_lines/optional_quest_line.png";
    public static final DependencyLineSettings DEFAULT = new DependencyLineSettings(true, "");

    public DependencyLineSettings {
        if (asset == null || (!asset.isEmpty() && ResourceLocation.tryParse(asset) == null)) {
            asset = "";
        }
    }

    public boolean isDefault() {
        return visible && asset.isEmpty();
    }

    public Optional<ResourceLocation> assetLocation() {
        return Optional.ofNullable(asset.isEmpty() ? null : ResourceLocation.tryParse(asset));
    }
}
