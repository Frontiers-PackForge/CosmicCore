package com.ghostipedia.cosmiccore.common.compat.ftbquests;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record DependencyLineSettings(boolean visible, String asset, long targetLinkId) {

    public static final String MAIN_QUESTLINE_ASSET = "cosmiccore:textures/gui/ftbquests/dependency_lines/main_quest_line.png";
    public static final String OFFROAD_ASSET = "cosmiccore:textures/gui/ftbquests/dependency_lines/optional_quest_line.png";
    public static final DependencyLineSettings DEFAULT = new DependencyLineSettings(true, "", 0L);

    public DependencyLineSettings(boolean visible, String asset) {
        this(visible, asset, 0L);
    }

    public DependencyLineSettings {
        if (asset == null || (!asset.isEmpty() && ResourceLocation.tryParse(asset) == null)) {
            asset = "";
        }
    }

    public boolean isDefault() {
        return visible && asset.isEmpty() && targetLinkId == 0L;
    }

    public DependencyLineSettings withVisible(boolean value) {
        return new DependencyLineSettings(value, asset, targetLinkId);
    }

    public DependencyLineSettings withAsset(String value) {
        return new DependencyLineSettings(visible, value, targetLinkId);
    }

    public DependencyLineSettings withTargetLinkId(long value) {
        return new DependencyLineSettings(visible, asset, value);
    }

    public Optional<ResourceLocation> assetLocation() {
        return Optional.ofNullable(asset.isEmpty() ? null : ResourceLocation.tryParse(asset));
    }
}
