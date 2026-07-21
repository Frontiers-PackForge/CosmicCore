package com.ghostipedia.cosmiccore.common.compat.ftbquests;

public enum DependencyLineStyle {

    DEFAULT,
    SOLID,
    DASHED,
    DOTTED;

    public static DependencyLineStyle byName(String name) {
        for (DependencyLineStyle style : values()) {
            if (style.name().equalsIgnoreCase(name)) return style;
        }
        return DEFAULT;
    }
}
