package com.ghostipedia.cosmiccore.common.compat.ftbquests;

public record DependencyLineSettings(boolean visible, DependencyLineStyle style) {

    public static final DependencyLineSettings DEFAULT = new DependencyLineSettings(true,
            DependencyLineStyle.DEFAULT);

    public boolean isDefault() {
        return visible && style == DependencyLineStyle.DEFAULT;
    }
}
